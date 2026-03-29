import os
import json
import subprocess

def get_models():
    api_key = os.getenv("GROQ_API_KEY")
    if not api_key:
        print("Please set the GROQ_API_KEY environment variable.")
        return
    cmd = [
        "curl.exe", "-s", "-X", "GET",
        "https://api.groq.com/openai/v1/models",
        "-H", f"Authorization: Bearer {api_key}"
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    try:
        data = json.loads(result.stdout)
        ids = [m['id'] for m in data.get('data', [])]
        for model_id in ids:
            print(model_id)
    except Exception as e:
        print(f"Error parsing JSON: {e}")
        print(f"Stdout: {result.stdout}")

if __name__ == "__main__":
    get_models()
