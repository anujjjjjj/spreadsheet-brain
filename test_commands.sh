#!/bin/bash

echo "Testing Spreadsheet Brain commands..."
echo ""
echo "Note: Replace SPREADSHEET_ID, PROJECT_ID, and GEMINI_API_KEY with your actual values"
echo ""

# Test 1: List sheets command
echo "Test 1: ask Show me all sheets in this spreadsheet"
echo "ask Show me all sheets in this spreadsheet" | mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" -Dexec.args="-s SPREADSHEET_ID --project-id PROJECT_ID --gemini-api-key GEMINI_API_KEY"

echo ""
echo "Test 2: ask Show me cells in the Sales sheet"
echo "ask Show me cells in the Sales sheet" | mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" -Dexec.args="-s SPREADSHEET_ID --project-id PROJECT_ID --gemini-api-key GEMINI_API_KEY"

echo ""
echo "Test 3: ask What cells depend on A1"
echo "ask What cells depend on A1" | mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" -Dexec.args="-s SPREADSHEET_ID --project-id PROJECT_ID --gemini-api-key GEMINI_API_KEY"

echo ""
echo "Test 4: Impact analysis"
echo "impact Sales!B2" | mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" -Dexec.args="-s SPREADSHEET_ID --project-id PROJECT_ID --gemini-api-key GEMINI_API_KEY"

echo ""
echo "All tests completed!"
echo "Remember to replace SPREADSHEET_ID, PROJECT_ID, and GEMINI_API_KEY with your actual values" 