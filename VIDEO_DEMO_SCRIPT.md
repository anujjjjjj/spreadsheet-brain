# Spreadsheet Brain - Video Demo Script

## 🎬 Demo Overview
**Duration**: 8-10 minutes  
**Target Audience**: Superjoin team  
**Demo Type**: Feature showcase with live demonstrations

---

## 📋 Pre-Demo Setup

### Required Items
- [ ] Terminal/Command Prompt ready
- [ ] Spreadsheet Brain application compiled and ready
- [ ] Test spreadsheet loaded (use provided test sheets)
- [ ] Screen recording software active
- [ ] Clear terminal window
- [ ] All credentials configured

### Test Spreadsheet
Use: `1q3-iEbNfAur8HB9zlW6-cw04KgdqWZznIi7Ublil5UI` (Sales, Employees, Inventory sheets)

---

## 🎥 Demo Script

### **Scene 1: Introduction (0:00 - 1:00)**

**[Screen: Title slide or clean terminal]**

**Narrator**: "Welcome to Spreadsheet Brain - a prototype knowledge graph engine that transforms Google Sheets into intelligent, queryable data systems. Today, I'll demonstrate how AI can deeply understand spreadsheets as living systems of data, formulas, and user behavior."

**[Show project structure briefly]**
```bash
ls -la
# Show the clean project structure
```

**Narrator**: "This is a Java-based system that builds a real-time knowledge graph from spreadsheet data, tracks cell dependencies, and provides AI-powered natural language querying."

---

### **Scene 2: Application Startup (1:00 - 2:00)**

**[Screen: Terminal with startup process]**

**Narrator**: "Let's start the application and load our test spreadsheet."

```bash
mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" \
  -Dexec.args="-s 1q3-iEbNfAur8HB9zlW6-cw04KgdqWZznIi7Ublil5UI --project-id spreadsheet-brain-123456 --gemini-api-key YOUR_API_KEY"
```

**[Wait for startup logs to appear]**

**Narrator**: "The system is now loading all sheets from our test spreadsheet. Notice how it's building the knowledge graph, parsing formulas, and establishing cross-sheet dependencies."

**[Point out key log messages]**
- "Loading all sheets from spreadsheet"
- "Building cross-sheet dependencies"
- "Graph Summary: 75 cells, 3 sheets, 17 formulas, 242 edges"

**Narrator**: "Perfect! We now have a knowledge graph with 75 cells across 3 sheets, 17 formulas, and 242 dependency relationships."

---

### **Scene 3: Multi-Sheet Support Demo (2:00 - 3:30)**

**[Screen: Interactive commands]**

**Narrator**: "First, let's explore the multi-sheet capabilities. Our test spreadsheet has three sheets: Sales, Employees, and Inventory."

```bash
ask Show me all sheets in this spreadsheet
```

**[Show response]**

**Narrator**: "Great! Now let's see what cells are in each sheet."

```bash
ask Show me cells in the Sales sheet
```

**[Show response]**

```bash
ask Show me cells in the Employees sheet
```

**[Show response]**

**Narrator**: "Notice how the system can query across multiple sheets seamlessly. This is the foundation of our multi-sheet support."

---

### **Scene 4: Natural Language Querying (3:30 - 5:00)**

**[Screen: Various natural language queries]**

**Narrator**: "Now let's demonstrate the AI-powered natural language querying. The system uses Google Gemini AI to understand and process natural language questions."

```bash
ask Find cells containing formulas
```

**[Show response with formula cells]**

**Narrator**: "The AI understands what we're asking and finds all cells with formulas across all sheets."

```bash
ask What cells depend on A1
```

**[Show response]**

**Narrator**: "The AI translates this natural language question into a structured query about dependencies."

```bash
ask Find cells with financial data
```

**[Show response]**

**Narrator**: "The AI can even understand semantic concepts like 'financial data' and identify relevant cells."

---

### **Scene 5: Impact Analysis Demo (5:00 - 6:30)**

**[Screen: Impact analysis commands]**

**Narrator**: "One of the most powerful features is impact analysis. Let's see what happens when we change specific cells."

```bash
impact Sales!B2
```

**[Show response with affected cells]**

**Narrator**: "This shows all cells that would be affected if we change Sales!B2. Notice how it includes cells from other sheets due to cross-sheet dependencies."

```bash
deps Employees!D3
```

**[Show response]**

**Narrator**: "This shows what the Employees!D3 cell depends on. The system tracks both direct and transitive dependencies."

```bash
ask What will be the impact if I change the formula in Sales!E2
```

**[Show response]**

**Narrator**: "The AI can understand complex impact analysis questions and provide detailed responses about the ripple effects of changes."

---

### **Scene 6: Cross-Sheet Dependencies (6:30 - 7:30)**

**[Screen: Complex formula analysis]**

**Narrator**: "Let's examine the complex cross-sheet dependencies. Our system can handle formulas that reference cells across multiple sheets."

```bash
ask Show me the formula in Sales!E2
```

**[Show response]**

**Narrator**: "This cell has the formula `=VLOOKUP(C2,Employees!A:B,2,FALSE)`, which references the Employees sheet."

```bash
ask What cells in the Employees sheet are referenced by Sales formulas
```

**[Show response]**

**Narrator**: "The system can identify all cross-sheet references and show how sheets are interconnected."

---

### **Scene 7: Cell Updates and Live Sync (7:30 - 8:30)**

**[Screen: Cell update demonstration]**

**Narrator**: "Let's demonstrate how the system handles cell updates and maintains synchronization."

```bash
ask Update cell Sales!A1 to "Updated Product Name"
```

**[Show response]**

**Narrator**: "The system can update cells through natural language commands."

```bash
update Sales!B2 150
```

**[Show response]**

**Narrator**: "We can also use direct update commands. The system maintains the knowledge graph in real-time."

---

### **Scene 8: Advanced Queries (8:30 - 9:30)**

**[Screen: Complex query examples]**

**Narrator**: "Let's try some more advanced queries to show the system's capabilities."

```bash
ask Find all cells that depend on cells containing "Revenue"
```

**[Show response]**

**Narrator**: "The AI can understand semantic relationships and find cells based on content."

```bash
ask Show me the dependency chain for the total calculation
```

**[Show response]**

**Narrator**: "The system can trace complex dependency chains across multiple sheets."

---

### **Scene 9: System Summary and Architecture (9:30 - 10:00)**

**[Screen: System summary]**

```bash
summary
```

**[Show graph summary]**

**Narrator**: "Let's see a summary of our knowledge graph. We have 75 cells across 3 sheets, with 17 formulas and 242 dependency relationships."

**[Show architecture briefly]**

**Narrator**: "This demonstrates the power of our knowledge graph architecture. The system has successfully:"

**[List key achievements]**
- ✅ **Multi-sheet support** with cross-sheet dependencies
- ✅ **AI-powered natural language querying**
- ✅ **Comprehensive impact analysis**
- ✅ **Real-time synchronization**
- ✅ **Complex formula parsing**

---

### **Scene 10: Conclusion and Future (10:00 - 10:30)**

**[Screen: Clean terminal or summary slide]**

**Narrator**: "Spreadsheet Brain successfully demonstrates how AI can transform traditional spreadsheets into intelligent, queryable knowledge systems."

**[Key takeaways]**
- **Knowledge Graph Architecture**: Models spreadsheet data as interconnected nodes
- **AI Integration**: Natural language interface with intelligent processing
- **Multi-Sheet Support**: Seamless handling of complex spreadsheets
- **Real-time Capabilities**: Live synchronization and updates
- **Extensible Design**: Easy to add new features and query types

**Narrator**: "This prototype provides a solid foundation for building enterprise-scale spreadsheet intelligence systems. The architecture supports scaling to handle larger spreadsheets, multiple users, and advanced AI features."

---

## 🎬 Production Notes

### **Camera/Recording Setup**
- **Screen Recording**: Full screen or terminal window
- **Audio**: Clear narration with good microphone
- **Pacing**: Allow time for responses to appear
- **Transitions**: Smooth transitions between scenes

### **Key Moments to Highlight**
1. **Startup Process**: Show the graph building in action
2. **Multi-Sheet Loading**: Demonstrate cross-sheet capabilities
3. **AI Responses**: Show natural language understanding
4. **Impact Analysis**: Demonstrate dependency tracking
5. **Cross-Sheet Dependencies**: Show complex formula handling
6. **Real-time Updates**: Demonstrate live synchronization

### **Troubleshooting**
- **If AI fails**: Use fallback processing examples
- **If slow response**: Explain it's processing complex dependencies
- **If error occurs**: Show graceful error handling

### **Demo Tips**
- **Speak clearly**: Explain what's happening at each step
- **Highlight features**: Point out key capabilities
- **Show results**: Let responses appear fully before continuing
- **Be prepared**: Have backup examples ready
- **Keep it professional**: Maintain a business-appropriate tone

---

## 📝 Demo Checklist

### **Pre-Demo**
- [ ] Application compiled and tested
- [ ] Test spreadsheet accessible
- [ ] All credentials configured
- [ ] Screen recording software ready
- [ ] Script reviewed and practiced

### **During Demo**
- [ ] Clear introduction
- [ ] Multi-sheet loading shown
- [ ] Natural language queries demonstrated
- [ ] Impact analysis showcased
- [ ] Cross-sheet dependencies explained
- [ ] Cell updates demonstrated
- [ ] Advanced queries shown
- [ ] System summary displayed
- [ ] Professional conclusion

### **Post-Demo**
- [ ] Video quality checked
- [ ] Audio clarity verified
- [ ] All features demonstrated
- [ ] Professional presentation maintained

---

**This script provides a comprehensive demonstration of all Spreadsheet Brain features while maintaining a professional, business-appropriate presentation style suitable for the Superjoin assignment submission.** 