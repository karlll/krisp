SCRIPT_DIR="$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

java -jar "${SCRIPT_DIR}/../build/libs/krisp-0.0.1-SNAPSHOT.jar"

