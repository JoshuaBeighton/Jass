import json
import requests
from pathlib import Path

# Configuration
SERVER_URL = "http://localhost:9000/player"
HEADERS = {
    "Content-Type": "application/json"
}

def post_json_file(file_path: Path):
    try:
        with file_path.open("r", encoding="utf-8") as f:
            data = json.load(f)

        response = requests.post(
            SERVER_URL,
            json=data,
            headers=HEADERS,
            timeout=10
        )

        response.raise_for_status()
        print(f"✅ Successfully posted {file_path.name} (status {response.status_code})")

    except Exception as e:
        print(f"❌ Failed to post {file_path.name}: {e}")

def main():
    for i in range(1, 3):
        file_path = Path(f"p{i}.json")
        if not file_path.exists():
            print(f"⚠️ File not found: {file_path.name}")
            continue

        post_json_file(file_path)

if __name__ == "__main__":
    main()
