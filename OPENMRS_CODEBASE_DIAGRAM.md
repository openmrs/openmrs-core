# OpenMRS Codebase Architecture & Features Diagram

## 🏗️ **High-Level Architecture**

```
┌─────────────────────────────────────────────────────────────────┐
│                    OpenMRS Core (3.0.0-SNAPSHOT)                │
│                    Multi-Module Maven Project                   │
└─────────────────────────────────────────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        │                       │                       │
        ▼                       ▼                       ▼
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│     API      │        │     Web      │        │   WebApp     │
│  (Core Logic)│        │ (Controllers)│        │ (Frontend UI)│
└──────────────┘        └──────────────┘        └──────────────┘
        │                       │                       │
        ▼                       ▼                       ▼
┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   Tools      │        │   Liquibase  │        │    Test      │
│(Build Tools) │        │(Database)    │        │(Testing)     │
└──────────────┘        └──────────────┘        └──────────────┘
```

## 📦 **Module Breakdown**

### **1. API Module** (`api/`)
**Purpose**: Core business logic and data models
**Key Features**:
- **Patient Management**: Patient, PatientIdentifier, PatientProgram
- **Person Management**: Person, PersonName, PersonAddress, PersonAttribute
- **Clinical Data**: Encounter, Obs (Observations), Order, TestOrder
- **User Management**: User, Role, Privilege, Provider
- **Location Management**: Location, LocationTag, LocationAttribute
- **Form Management**: Form, FormField, FormResource
- **Program Management**: Program, ProgramWorkflow, ProgramWorkflowState
- **Order Management**: Order, OrderSet, OrderGroup, OrderType
- **Medication**: MedicationDispense, DosingInstructions
- **HL7 Integration**: HL7 message processing
- **Custom Data Types**: Extensible attribute system
- **Validation**: Data validation and business rules
- **Serialization**: Data export/import capabilities

### **2. Web Module** (`web/`)
**Purpose**: Web layer controllers and servlets
**Key Features**:
- **Web Controllers**: REST API endpoints
- **Servlets**: DispatcherServlet, StaticDispatcherServlet
- **Filters**: Request/response filtering
- **Web Utilities**: WebUtil, WebConstants
- **Binding**: OpenmrsBindingInitializer
- **Web Daemon**: Background web services

### **3. WebApp Module** (`webapp/`)
**Purpose**: Frontend user interface and web resources
**Key Features**:
- **Static Resources**: Images, CSS, JavaScript
- **Web Pages**: HTML templates and views
- **Configuration**: WEB-INF configuration files
- **Error Handling**: Error pages and error handling
- **Initial Setup**: Installation and setup pages

### **4. Liquibase Module** (`liquibase/`)
**Purpose**: Database schema management and migrations
**Key Features**:
- **Database Migrations**: Version-controlled schema changes
- **Snapshot Management**: Database state snapshots
- **Schema Evolution**: Automatic database updates
- **Data Integrity**: Referential integrity maintenance

### **5. Tools Module** (`tools/`)
**Purpose**: Build and development utilities
**Key Features**:
- **Build Tools**: Maven plugins and utilities
- **Code Generation**: Template and code generators
- **Development Utilities**: Helper scripts and tools

### **6. Test Module** (`test/`)
**Purpose**: Testing framework and utilities
**Key Features**:
- **Test Framework**: JUnit-based testing
- **Test Data**: Sample data and fixtures
- **Mock Objects**: Test doubles and mocks
- **Integration Tests**: End-to-end testing

## 🔧 **Core Features by Domain**

### **🏥 Clinical Care**
```
Patient Management
├── Patient Registration
├── Patient Identification
├── Patient Programs
└── Patient States

Clinical Data
├── Encounters
├── Observations (Obs)
├── Orders & Test Orders
├── Medications
└── Forms & Form Fields

Care Programs
├── Program Enrollment
├── Workflow Management
├── State Transitions
└── Outcome Tracking
```

### **👥 User & Access Management**
```
User System
├── User Accounts
├── Role-Based Access
├── Privileges
├── Provider Management
└── Session Management

Security
├── Authentication
├── Authorization
├── Audit Logging
└── Data Privacy
```

### **🏢 Organization & Location**
```
Location Management
├── Physical Locations
├── Location Tags
├── Location Attributes
└── Hierarchical Structure

Provider Management
├── Healthcare Providers
├── Provider Roles
├── Provider Attributes
└── Service Assignments
```

### **📊 Data & Integration**
```
Data Management
├── Custom Data Types
├── Extensible Attributes
├── Data Validation
└── Business Rules

Integration
├── HL7 Message Processing
├── REST API Services
├── Data Import/Export
└── External System Connectors
```

## 🎯 **Good First Issue Areas**

### **1. Documentation (Beginner-Friendly)**
- **README files** in various modules
- **JavaDoc comments** in source code
- **API documentation** updates
- **Code examples** and tutorials

### **2. Testing (Good for Learning)**
- **Unit test coverage** improvements
- **Test data** creation
- **Integration test** scenarios
- **Performance test** cases

### **3. Code Quality (Intermediate)**
- **Code formatting** and style
- **Static analysis** fixes
- **Code review** suggestions
- **Refactoring** opportunities

### **4. UI/UX (Frontend Focused)**
- **Web interface** improvements
- **Form validation** enhancements
- **User experience** refinements
- **Accessibility** improvements

### **5. Data & Validation (Backend Focused)**
- **Data validation** rules
- **Business logic** improvements
- **Error handling** enhancements
- **Performance** optimizations

## 🚀 **Getting Started Recommendations**

### **For Complete Beginners:**
1. **Start with documentation** - README files, JavaDoc comments
2. **Look for "good first issue" labels** on GitHub/JIRA
3. **Focus on test files** to understand the codebase
4. **Ask questions** in the community

### **For Developers with Some Experience:**
1. **Pick a specific domain** (e.g., Patient Management, Forms)
2. **Look for bug fixes** in your chosen area
3. **Improve test coverage** for existing features
4. **Enhance validation** and error handling

### **For Experienced Developers:**
1. **Feature development** in core areas
2. **Performance optimization** opportunities
3. **Architecture improvements** and refactoring
4. **Integration enhancements** and new APIs

## 🔍 **Finding Issues to Work On**

### **GitHub Issues:**
- **URL**: https://github.com/openmrs/openmrs-core/issues
- **Labels to look for**: `good first issue`, `help wanted`, `documentation`

### **JIRA Issues:**
- **URL**: https://tickets.openmrs.org/
- **Project**: TRUNK (OpenMRS Core)
- **Search**: `labels = "good first issue" AND status = Open`

### **Community Resources:**
- **Developer Guide**: http://om.rs/newdev
- **Wiki**: https://wiki.openmrs.org/
- **Talk Forum**: https://talk.openmrs.org/
- **Mailing Lists**: http://go.openmrs.org/lists

## 📈 **Contribution Workflow**

```
1. Find Issue → 2. Fork Repo → 3. Create Branch → 4. Make Changes
    ↓
5. Test Changes → 6. Commit & Push → 7. Create PR → 8. Code Review
    ↓
9. Address Feedback → 10. Merge & Celebrate! 🎉
```

---

*This diagram provides a high-level overview of the OpenMRS codebase. Use it to understand the architecture and identify areas where you'd like to contribute. Remember to start small and ask questions - the OpenMRS community is very welcoming to newcomers!*
