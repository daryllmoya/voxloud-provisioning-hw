#!/bin/zsh

echo '--- Desk phone without override fragment ---'
curl -sX GET http://localhost:8080/api/v1/provisioning/aa-bb-cc-dd-ee-ff | cat
echo ""
echo ""

echo '--- Conference phone without override fragment ---'
curl -sX GET http://localhost:8080/api/v1/provisioning/f1-e2-d3-c4-b5-a6 | jq '.'
echo ""

echo '--- Desk phone with override fragment ---'
curl -sX GET http://localhost:8080/api/v1/provisioning/a1-b2-c3-d4-e5-f6 | cat
echo ""
echo ""

echo '--- Conference phone with override fragment ---'
curl -sX GET http://localhost:8080/api/v1/provisioning/1a-2b-3c-4d-5e-6f | jq '.'
echo ""

echo '--- Not existing device ---'
curl -s -w "HTTP status: %{http_code}\n" http://localhost:8080/api/v1/provisioning/aa-aa-aa-aa-aa-aa
