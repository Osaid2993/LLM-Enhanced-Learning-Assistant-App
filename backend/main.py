import os
from flask import Flask, request, jsonify
import re
from groq import Groq

os.environ['GROQ_API_KEY'] = 'YOUR_GROQ_API_KEY'

app = Flask(__name__)

# Initialize the Groq client
client = Groq()
MODEL_NAME = "llama-3.1-8b-instant"


def askLlama(prompt, max_tokens=500):
    response = client.chat.completions.create(
        model=MODEL_NAME,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=max_tokens,
        temperature=0.7,
    )
    return response.choices[0].message.content


def fetchQuizFromLlama(student_topic):
    print("Fetching quiz from llama")
    query = (
        f"Generate a quiz with 3 questions to test students on the provided topic. "
        f"For each question, generate 4 options where only one of the options is correct. "
        f"Format your response as follows:\n"
        f"QUESTION: [Your question here]?\n"
        f"OPTION A: [First option]\n"
        f"OPTION B: [Second option]\n"
        f"OPTION C: [Third option]\n"
        f"OPTION D: [Fourth option]\n"
        f"ANS: [Correct answer letter]\n\n"
        f"Ensure text is properly formatted. It needs to start with a question, then the options, and finally the correct answer. "
        f"Follow this pattern for all questions. Do not include any other text, intro, or closing remarks. "
        f"Here is the student topic:\n{student_topic}"
    )
    response = askLlama(query, max_tokens=800)
    print(response)
    return response


def process_quiz(quiz_text):
    questions = []

    # Split the response into blocks, one per question
    blocks = re.split(r'(?=QUESTION:)', quiz_text, flags=re.IGNORECASE)

    for block in blocks:
        block = block.strip()
        if not block:
            continue

        q_match = re.search(r'QUESTION:\s*(.+?)(?=\n\s*OPTION A:)', block, re.IGNORECASE | re.DOTALL)
        a_match = re.search(r'OPTION A:\s*(.+?)(?=\n\s*OPTION B:)', block, re.IGNORECASE | re.DOTALL)
        b_match = re.search(r'OPTION B:\s*(.+?)(?=\n\s*OPTION C:)', block, re.IGNORECASE | re.DOTALL)
        c_match = re.search(r'OPTION C:\s*(.+?)(?=\n\s*OPTION D:)', block, re.IGNORECASE | re.DOTALL)
        d_match = re.search(r'OPTION D:\s*(.+?)(?=\n\s*ANS:)', block, re.IGNORECASE | re.DOTALL)
        ans_match = re.search(r'ANS:\s*([A-D])', block, re.IGNORECASE)

        if not (q_match and a_match and b_match and c_match and d_match and ans_match):
            continue

        question_data = {
            "question": q_match.group(1).strip(),
            "options": [
                a_match.group(1).strip(),
                b_match.group(1).strip(),
                c_match.group(1).strip(),
                d_match.group(1).strip(),
            ],
            "correct_answer": ans_match.group(1).strip().upper()
        }
        questions.append(question_data)

    return questions


def fetchHintFromLlama(question, options):
    print("Fetching hint from llama")
    options_text = ""
    for i, opt in enumerate(options):
        options_text += f"OPTION {chr(65 + i)}: {opt}\n"

    query = (
        f"A student is stuck on the following multiple-choice question. "
        f"Give a short hint in 2 to 3 sentences that helps them think through the problem. "
        f"Do NOT reveal which option is correct. Do not mention option letters. "
        f"Only output the hint text, nothing else.\n\n"
        f"QUESTION: {question}\n"
        f"{options_text}"
    )
    response = askLlama(query, max_tokens=200)
    print(response)
    return response.strip()


def fetchExplanationFromLlama(question, options, correct_answer, user_answer):
    print("Fetching explanation from llama")
    options_text = ""
    for i, opt in enumerate(options):
        options_text += f"OPTION {chr(65 + i)}: {opt}\n"

    is_correct = user_answer.strip().upper() == correct_answer.strip().upper()
    verdict = "correct" if is_correct else "incorrect"
    user_ans_text = user_answer if user_answer else "no answer given"

    query = (
        f"A student answered the following multiple-choice question. "
        f"The student was {verdict}. In 2 to 4 sentences, explain why the correct answer is right. "
        f"If the student was incorrect, also briefly explain why their choice was wrong. "
        f"Keep the tone friendly and clear. Only output the explanation text, nothing else.\n\n"
        f"QUESTION: {question}\n"
        f"{options_text}"
        f"CORRECT ANSWER: {correct_answer}\n"
        f"STUDENT ANSWER: {user_ans_text}\n"
    )
    response = askLlama(query, max_tokens=300)
    print(response)
    return response.strip(), is_correct


@app.route('/getQuiz', methods=['GET'])
def get_quiz():
    print("Request received")
    student_topic = request.args.get('topic')
    if student_topic is None:
        return jsonify({'error': 'Missing topic parameter'}), 400
    quiz = fetchQuizFromLlama(student_topic)
    return jsonify({'quiz': process_quiz(quiz)}), 200


@app.route('/getHint', methods=['POST'])
def get_hint():
    print("Hint request received")
    body = request.get_json(silent=True) or {}
    question = body.get('question')
    options = body.get('options', [])

    if not question:
        return jsonify({'error': 'Missing question parameter'}), 400

    hint = fetchHintFromLlama(question, options)
    return jsonify({'hint': hint}), 200


@app.route('/explainAnswer', methods=['POST'])
def explain_answer():
    print("Explain request received")
    body = request.get_json(silent=True) or {}
    question = body.get('question')
    options = body.get('options', [])
    correct_answer = body.get('correct_answer')
    user_answer = body.get('user_answer', '')

    if not question or not correct_answer:
        return jsonify({'error': 'Missing question or correct_answer parameter'}), 400

    explanation, is_correct = fetchExplanationFromLlama(question, options, correct_answer, user_answer)
    return jsonify({
        'explanation': explanation,
        'is_correct': is_correct
    }), 200


@app.route('/test', methods=['GET'])
def run_test():
    return jsonify({'quiz': "test"}), 200

def fetchSummaryFromLlama(questions_text):
    print("Fetching summary from llama")
    query = (
        f"A student got the following questions wrong in their quizzes. "
        f"Give a brief, encouraging summary of the areas they need to improve on. "
        f"Group similar topics together if possible. "
        f"Keep it to 3 to 5 sentences. Be specific about what concepts they should review. "
        f"Only output the summary text, nothing else.\n\n"
        f"{questions_text}"
    )
    response = askLlama(query, max_tokens=300)
    print(response)
    return response.strip()


@app.route('/summarize', methods=['POST'])
def summarize():
    print("Summary request received")
    body = request.get_json(silent=True) or {}
    questions_text = body.get('questions')

    if not questions_text:
        return jsonify({'error': 'Missing questions parameter'}), 400

    summary = fetchSummaryFromLlama(questions_text)
    return jsonify({'summary': summary}), 200

if __name__ == '__main__':
    port_num = 5000
    print(f"App running on port {port_num}")
    app.run(port=port_num, host="0.0.0.0")
