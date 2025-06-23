# Spreadsheet Brain

> **A powerful knowledge graph engine for Google Sheets that transforms spreadsheets into intelligent, queryable data systems**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## Project Overview

Spreadsheet Brain is a prototype system that demonstrates how AI can deeply understand spreadsheets as living systems of data, formulas, and user behavior. It builds a real-time knowledge graph that tracks cell dependencies, supports AI-driven natural language queries, and provides comprehensive impact analysis across multiple sheets.

### Key Features

- **🤖 AI-Powered Queries**: Natural language interface powered by Google Gemini AI
- **📊 Multi-Sheet Support**: Analyze dependencies across multiple sheets in a single spreadsheet
- **🔗 Dependency Tracking**: Visualize how changes to one cell affect others
- **📈 Impact Analysis**: See the ripple effects of changing any cell
- **🔄 Real-time Updates**: Live sync mode for continuous monitoring
- **🌐 Cross-Sheet References**: Handle complex formulas that reference cells in other sheets
- **📝 Formula Analysis**: Understand complex spreadsheet formulas and their dependencies

## Requirements Fulfillment

### ✅ **1. Natural Language Querying and Updates (Smart Questions)**

**Implemented Features:**
- **AI Integration**: Google Gemini API integration with intelligent query processing
- **Natural Language Support**: Handles queries like:
  - `"Show all revenue cells connected to marketing campaigns"`
  - `"List all formulas that reference the Revenue column"`
  - `"What will be affected if I change cell B5?"`
- **Fallback Processing**: Rule-based processing when AI is unavailable
- **Multi-Modal Queries**: Cell discovery, dependency analysis, impact analysis, formula listing

**Example Usage:**
```bash
ask Show me cells in the Sales sheet
ask Find cells containing formulas
ask What cells depend on A1?
ask Update cell A1 to 100
```

### ✅ **2. Impact Analysis**

**Implemented Features:**
- **Dependency Tracking**: `analyzeImpact()` method tracks downstream effects
- **Transitive Dependencies**: `getTransitiveDependents()` finds all affected cells
- **Cross-Sheet Impact**: Handles dependencies across multiple sheets
- **Real-time Updates**: Live sync mode for continuous monitoring

**Example Usage:**
```bash
impact A1                    # Show all cells affected by A1
impact Sales!B5              # Cross-sheet impact analysis
deps C10                     # Show what C10 depends on
```

### ✅ **3. AI Integration**

**Implemented Features:**
- **Query Interface**: CLI with natural language support
- **Graph Queries**: AI can query the knowledge graph
- **Live Synchronization**: Real-time updates with Google Sheets
- **Extensible Framework**: Easy to add new query types

**AI Capabilities:**
- Natural language → Structured query translation
- Context-aware spreadsheet terminology understanding
- Intelligent fallback when AI is unavailable
- Multi-format query support

### ✅ **4. Multi-Sheet Support (Bonus Feature)**

**Implemented Features:**
- **Cross-Sheet References**: Handles formulas like `=SUMIF(Sales!C:C,A3,Sales!D:D)`
- **Sheet Management**: `loadAllSheets()` loads all sheets in a spreadsheet
- **Cross-Sheet Dependencies**: `parseCrossSheetDependencies()` builds edges across sheets
- **Column Range Support**: Handles ranges like `A:B`, `C:C`, `D:D`

**Example Cross-Sheet Dependencies:**
```
Sales!E2 (formula: =VLOOKUP(C2,Employees!A:B,2,FALSE))
├── Depends on: Employees!A1, Employees!A2, ..., Employees!B5
└── Creates: 10 dependency edges

Inventory!D2 (formula: =SUMIF(Sales!B:B,A2,Sales!D:D))
├── Depends on: Sales!B1, Sales!B2, ..., Sales!D1, Sales!D2, ...
└── Creates: 12 dependency edges
```

## ��️ Architecture

### Core Components

```
spreadsheet-brain/
├── src/main/java/com/superjoin/spreadsheet/
│   ├── Main.java                    # Application entry point
│   ├── SpreadsheetGraph.java        # Main orchestrator & formula parsing
│   ├── Cell.java                    # Cell data structure
│   ├── GeminiQueryService.java      # AI query processing
│   ├── SheetsReader.java            # Google Sheets API integration
│   ├── model/
│   │   ├── CellNode.java            # Graph node for cells
│   │   ├── SheetNode.java           # Graph node for sheets
│   │   └── GraphNode.java           # Base graph node
│   └── services/
│       ├── KnowledgeGraphService.java # Graph operations & traversal
│       └── SpreadsheetReader.java    # Data reading utilities
├── src/test/java/                   # Test suite
├── DESIGN.md                        # System architecture & design
├── SCALING.md                       # Scaling considerations
└── README.md                        # This file
```

### Design Principles

1. **Knowledge Graph Architecture**: Uses a graph database to model cell relationships
2. **AI-First Design**: Natural language queries with fallback rule-based processing
3. **Multi-Sheet Support**: Seamless handling of complex spreadsheets with multiple tabs
4. **Real-time Capabilities**: Live synchronization with continuous monitoring
5. **Extensible Framework**: Easy addition of new query types and analysis features

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Google Cloud Project** with enabled APIs:
  - Google Sheets API
  - Google Drive API
  - Generative Language API (Gemini)
- **Google Service Account** with appropriate permissions
- **Gemini API Key** for AI-powered queries

## 🔧 Setup

### 1. Google Cloud Setup

1. Create a Google Cloud Project
2. Enable the required APIs:
   ```bash
   gcloud services enable sheets.googleapis.com
   gcloud services enable drive.googleapis.com
   gcloud services enable generativelanguage.googleapis.com
   ```

3. Create a Service Account:
   ```bash
   gcloud iam service-accounts create spreadsheet-brain-sa
   gcloud projects add-iam-policy-binding YOUR_PROJECT_ID \
     --member="serviceAccount:spreadsheet-brain-sa@YOUR_PROJECT_ID.iam.gserviceaccount.com" \
     --role="roles/drive.readonly"
   ```

4. Download the service account key as `credentials.json`

### 2. Gemini API Setup

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create a new API key
3. Copy the API key (starts with `AIza...`)

### 3. Spreadsheet Permissions

1. Share your Google Spreadsheet with the service account email
2. Grant "Viewer" or "Editor" permissions as needed

## 🚀 Running the Application

### Build the Project

```bash
mvn clean compile
```

### Run with Maven

```bash
mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" \
  -Dexec.args="-s YOUR_SPREADSHEET_ID --project-id YOUR_PROJECT_ID --gemini-api-key YOUR_GEMINI_API_KEY"
```

### Run with JAR

```bash
# Package the application
mvn clean package

# Run the JAR
java -jar target/spreadsheet-brain-1.0-SNAPSHOT.jar \
  -s YOUR_SPREADSHEET_ID \
  --project-id YOUR_PROJECT_ID \
  --gemini-api-key YOUR_GEMINI_API_KEY
```

### Live Sync Mode

For continuous monitoring with automatic updates:

```bash
mvn exec:java -Dexec.mainClass="com.superjoin.spreadsheet.Main" \
  -Dexec.args="-s YOUR_SPREADSHEET_ID --project-id YOUR_PROJECT_ID --gemini-api-key YOUR_GEMINI_API_KEY --live-sync"
```

## 📖 Available Commands

### Interactive Commands

Once the application is running, you can use these commands:

#### **Sheet Management**
- `ask Show me all sheets` - Natural language version

#### **Cell Analysis**
- `impact <cell>` - Show all cells affected by changes to `<cell>`
  - Example: `impact A1` or `impact Sheet1!B5`
- `deps <cell>` - Show all cells that `<cell>` depends on
  - Example: `deps C10` or `deps Deals!F9`

#### **Cell Discovery**
- `ask Show me cells in <sheet>` - Show cells from a specific sheet
  - Example: `ask Show me cells in the Deals sheet`
- `ask Show me all cells containing formulas` - List formula cells
- `ask Find cells with dates` - Find date-related cells
- `ask Find cells with financial data` - Find financial data cells

#### **Cell Updates**
- `ask Update cell A1 to 100` - Natural language version

#### **General Queries**
- `ask <natural language query>` - Use AI-powered natural language queries
- `help` - Show available commands
- `quit` or `exit` - Exit the application

### Natural Language Examples

The AI can understand various query formats:

```
ask What is the impact of changing cell A1?
ask Show me all cells that depend on B5
ask List all cells in the Revenue sheet
ask Find cells containing formulas
ask What cells are affected if I change the price in cell D10?
ask Show me cells with dates in July
ask Update the total in cell F20 to 1500
```

## 🔍 Understanding the Output

### Impact Analysis
```
Impact analysis for A1: 3 cells affected
Cells affected:
  Sheet1!B5
  Sheet1!C10
  Revenue!D15
```

### Dependency Analysis
```
Dependency analysis for C10: 2 dependencies
Dependencies:
  Sheet1!A1
  Sheet1!B5
```

### Cell Discovery
```
Cells in sheet 'Deals':
1. A1: Product Name
2. B1: Price
3. C1: Quantity
4. A2: Widget A
5. B2: 100
...
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Test Specific Features
```bash
# Test multi-sheet functionality
mvn test -Dtest=TestMultiSheet

# Test live sync
mvn test -Dtest=TestLiveSync

# Test commands
mvn test -Dtest=TestCommands
```

### Manual Testing
Use the provided test script (update with your values first):
```bash
chmod +x test_commands.sh
./test_commands.sh
```

## 📊 Multi-Sheet Support Implementation

### Cross-Sheet Reference Parsing

The system handles complex cross-sheet references through sophisticated formula parsing:

```java
// Example: =SUMIF(Sales!C:C,A3,Sales!D:D)
// Creates dependencies from current cell to all cells in Sales!C:C and Sales!D:D
```

### Implementation Details

1. **Pattern Matching**: Uses regex patterns to identify cross-sheet references
   ```java
   Pattern CROSS_SHEET_PATTERN = Pattern.compile("([A-Za-z0-9_]+)!([A-Za-z0-9:]+)");
   ```

2. **Column Range Support**: Handles ranges like `A:B`, `C:C`, `D:D`
   - Parses start and end columns
   - Finds all cells within the range
   - Creates dependency edges for each cell

3. **Sheet Loading**: `loadAllSheets()` method loads all sheets simultaneously
   - Maintains sheet hierarchy
   - Preserves cross-sheet relationships
   - Enables unified querying across all sheets

4. **Dependency Building**: Two-phase approach
   - Phase 1: Build same-sheet dependencies
   - Phase 2: Build cross-sheet dependencies

## 🔧 Configuration

### Environment Variables
- `GOOGLE_APPLICATION_CREDENTIALS` - Path to service account credentials
- `GEMINI_API_KEY` - Your Gemini API key

### Maven Settings
Create a `settings.xml` file in your Maven configuration:
```xml
<settings>
    <profiles>
        <profile>
            <id>default</id>
            <repositories>
                <repository>
                    <id>central</id>
                    <url>https://repo1.maven.org/maven2/</url>
                </repository>
            </repositories>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>default</activeProfile>
    </activeProfiles>
</settings>
```

## 📈 Performance Characteristics

### Current System
- **Memory Usage**: Linear with cell count (O(n))
- **Query Performance**: O(V + E) for graph traversals
- **Update Performance**: O(n) for n affected cells
- **Scalability**: Tested with 75+ cells across 3 sheets

### Target Metrics
| Metric | Current | Target | Improvement |
|--------|---------|--------|-------------|
| Cells per Second | 100 | 10,000 | 100x |
| Memory per Cell | 1KB | 100B | 10x |
| Query Response Time | 500ms | 50ms | 10x |
| Concurrent Users | 1 | 1,000 | 1000x |
| Spreadsheet Size | 75 cells | 100,000 cells | 1333x |

## 🚀 Scaling Considerations

For detailed scaling strategy, see [SCALING.md](SCALING.md)

### Key Scaling Areas
1. **Performance Optimization**: Memory and processing improvements
2. **Database Integration**: Persistent graph storage
3. **Distributed Architecture**: Microservices and horizontal scaling
4. **Advanced Features**: Graph databases and ML integration

## 📚 Documentation

- **[DESIGN.md](DESIGN.md)**: Comprehensive system architecture and design
- **[SCALING.md](SCALING.md)**: Scaling strategy and considerations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request


## 🙏 Acknowledgments

- **Google Sheets API** for spreadsheet integration
- **Google Gemini AI** for natural language processing
- **Maven** for build management
- **SLF4J** for logging
