import os
import requests
from git import Repo
from dotenv import load_dotenv

def stage_changes(file_name, repo):
    try:
        if isinstance(file_name, str):
            repo.index.add(file_name)
        print(f"Successfully staged {file_name}.")
    except Exception as e:
        print(f"Failed to stage {file_name}. Error: {e}")
    
def commit_locally(filename, commit_message, repo):
    try:
        repo.git.add(filename)
        repo.git.commit('-m', commit_message)
        print(f"Successfully committed {filename}.")
    except Exception as e:
        print(f"Failed to commit {filename}. Error: {e}")
        
def push_changes(branch_name, repo):
    try:
        origin = repo.remote(name='origin')
        origin.push(refspec=f'{branch_name}:{branch_name}')
        print(f"Successfully pushed changes to {branch_name}.")
    except Exception as e:
        print(f"Failed to push changes to {branch_name}. Error: {e}")


# Create the pull request
def create_pull_request(api_key, repo_owner, repo_name, file_name, head_branch, base_branch, issue_content):
    url = f"https://api.github.com/repos/{repo_owner}/{repo_name}/pulls"
    headers = {
        "Authorization": f"token {api_key}",
        "Accept": "application/vnd.github.v3+json"
    }
    data = {
        "title": "Pull Request for: " + file_name,
        "body": issue_content,
        "head": head_branch,
        "base": base_branch
    }
        
    response = requests.post(url, headers=headers, json=data)
    if response.status_code == 201:
        print("Pull request created successfully.")
        print("Pull request URL:", response.json()['html_url'])
    else:
        print(response.status_code)
        print("Failed to create pull request.")
        print("Response:", response.json())
    
if __name__ == "__main__":
    
    load_dotenv()
    # Variables
    API_KEY = os.getenv('API_KEY')

    REPO_OWNER = os.getenv('REPO_OWNER')
    REPO_NAME = os.getenv('REPO_NAME')

    # HEAD_BRANCH_NAME and BASE_BRANCH_NAME are the branches you want to merge
    HEAD_BRANCH = os.getenv("HEAD_BRANCH_NAME") # The branch where your changes are made
    BASE_BRANCH = os.getenv("BASE_BRANCH_NAME") # The branch you want to merge your changes into, typically "main" or "master"

    FILE_NAME = os.getenv("FILE_NAME") # The modified file

    ###################### Setup the repository object ######################

    REPO_PATH = os.getcwd()
    
    # print(FILE_NAME)
    # print(REPO_PATH)
    # print(HEAD_BRANCH)
    # print(BASE_BRANCH)
    # print(REPO_OWNER)

    repo_obj = Repo(REPO_PATH)
    
    # Read issue information from file
    with open('issues.txt', 'r') as file:
        issue_content = file.read().strip()
        
    # Read commit message from file
    with open('commit.txt', 'r') as file:
        commit_message = file.read().strip()
        
    stage_changes(FILE_NAME, repo_obj)
        
    commit_locally(FILE_NAME, commit_message, repo_obj)
    
    push_changes(HEAD_BRANCH, repo_obj)
        
    create_pull_request(API_KEY, REPO_OWNER, REPO_NAME, HEAD_BRANCH, BASE_BRANCH, issue_content)
