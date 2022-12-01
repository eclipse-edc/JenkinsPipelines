# these should be parameters


curl="curl --fail -sSl -H 'Authorization: Bearer ${TOKEN}'"

echo "Starting new workflow '$WORKFLOW"
${curl} -X POST "https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/$WORKFLOW/dispatches" \
-H "Accept: application/vnd.github.v3+json" \
-d '{"ref": "main"}'

numRuns=0
while [ "$numRuns" -le "0" ]
do 
    echo "Waiting for ongoing runs"
    runs=$($curl -X GET "https://api.github.com/repos/${OWNER}/${REPO}/actions/workflows/${WORKFLOW}/runs?event=workflow_dispatch&status=in_progress")
    numRuns=$(echo $runs | jq -r '.total_count')
    echo " - found $numRuns"
    sleep 3
done

runId=$(echo $runs | jq -r '.workflow_runs[0].id')


echo "Checking run ID $runId"
while [ "$status" != "completed" ]
do
    
    json=$($curl -X GET "https://api.github.com/repos/${OWNER}/${REPO}/actions/runs/${runId}")
    status=$(echo $json | jq -r '.status')
    conclusion=$(echo $json | jq -r '.conclusion')

    echo "$(date) :: $runId is $status"
    sleep 5
done

echo "Run completed, conclusion: $conclusion"
