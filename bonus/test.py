from openai import OpenAI
import sys
from dotenv import load_dotenv
import os
import random
import PR
from git import Repo
from time import sleep

from prompt import SYSTEM, SMELL, REFACTOR

def refactor():
    with open(FILE_PATH, "r") as file:
        data = file.read().strip()

    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "system", "content": SYSTEM},
            {"role": "user", "content": f"{SMELL} {data}"},
        ],
    )
    
    smells = response.choices[0].message.content

    with open(f"bonus/{FILE_NAME}_smells.md", "w") as f:
        f.write(smells)

    print(f"Smells written to {FILE_NAME}_smells.md")

    response = client.chat.completions.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "system", "content": SYSTEM},
            {"role": "user", "content": f"{SMELL} {data}"},
            {"role": "assistant", "content": response.choices[0].message.content},
            {"role": "user", "content": REFACTOR},
        ],
    )

    code = response.choices[0].message.content
    
    code = "\n".join([line for line in code.split("\n") if "```" not in line])

    with open(FILE_PATH, "w") as f:
        f.write(code)
        print(f"{FILE_NAME} has been refactored.")
        
    return code, smells


if __name__ == "__main__":
    load_dotenv()

    OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

    API_KEY = os.getenv("API_KEY")

    REPO_OWNER = os.getenv("REPO_OWNER")
    REPO_NAME = os.getenv("REPO_NAME")

    BASE_BRANCH = os.getenv(
        "BASE_BRANCH_NAME"
    )

    # FILE_PATH = "books-core/src/main/java/com/sismics/books/core/dao/jpa/BookDao.java"
    FILE_PATH = sys.argv[1]
    FILE_NAME = FILE_PATH.split("/")[-1].split(".")[0]
    
    HEAD_BRANCH = f"modify_file_{FILE_NAME}_{random.randint(1, 1000)}"
    # BASE_BRANCH = "modify_file_BookDao.java_289"

    client = OpenAI(api_key=OPENAI_API_KEY)
    
    try:
        REPO_PATH = os.getcwd()
        repo = Repo(REPO_PATH)
    except Exception as e:
        print(f'Wrong REPO Path {REPO_PATH}')
        exit(1)
    
    try:
        new_branch = repo.create_head(HEAD_BRANCH)
        new_branch.checkout()
    except Exception as e:
        print(f"Failed to checkout {HEAD_BRANCH}. Error: {e}")
        exit(1)
        
    OLD_PATH = os.getcwd()
    llm_refactored = False
    
    for i in range(5):
        os.chdir(OLD_PATH)
        code, smells = refactor()
    
        os.chdir("books-parent")
        build_output = os.system("mvn clean -DskipTests install > /dev/null 2>&1")
        if build_output == 0:
            print("Build Success.")
            llm_refactored = True
            break
        else:
            print("Build Failed.")

    
    PR.stage_changes(FILE_PATH, repo)
    
    PR.commit_locally(FILE_PATH, f"Refactor design and code smells in {FILE_NAME}", repo)
    
    if(not llm_refactored):
        repo.git.checkout(BASE_BRANCH)
        repo.delete_head(HEAD_BRANCH, force=True)
        print("LLM refactoring failed. Exiting.")
        exit(1)

    PR.push_changes(HEAD_BRANCH, repo)
    
    sleep(5)
    
    PR.create_pull_request(API_KEY, REPO_OWNER, REPO_NAME, FILE_NAME, HEAD_BRANCH, BASE_BRANCH, smells)
    
    repo.git.checkout(BASE_BRANCH)