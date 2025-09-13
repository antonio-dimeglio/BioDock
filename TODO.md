# BioDock Development TODO

*Organized development roadmap based on current state analysis and recent design decisions*

## 🎯 Current Priority: Core Pipeline Execution (Phase 1)

### Critical Fixes & Updates
- [ ] **Fix DockerService volume mounting** 
  - Fix missing `-` in volume flags (`-v` instead of `v`)  
  - Update volume paths to match new design (`/data` for input, `/results` for output)
  - Complete `runPipeline()` method implementation (currently has TODO)
  - Add proper error handling and logging for docker run execution

- [ ] **Update UI for Flexible Input System**
  - Implement folder selection dialog alongside current drag & drop
  - Create input handler that supports both file drag/drop AND folder selection  
  - Implement symlink creation for dragged files (avoid copying large genomics files)
  - Update MainController to handle both input methods
  - Add UI indicators showing whether user selected files or folder

- [ ] **Pipeline Model Cleanup** ✅ *Already Done*
  - Pipeline model already matches simplified design
  - Remove any lingering references to old input/output directory fields in other files

### Core Functionality Implementation  
- [ ] **Complete Docker Integration**
  - Implement `fetchContainerStatus()` method
  - Add container cleanup after execution
  - Implement proper docker build caching and cleanup
  - Add support for streaming docker build/run output to UI

- [ ] **Result Processing System**
  - Create result collection from `/results/` volume mount
  - Implement basic result viewer/browser in UI
  - Add result file type detection and handling
  - Create result archiving and project persistence

- [ ] **Error Handling & User Feedback**
  - Implement comprehensive error dialogs for pipeline failures
  - Add progress indicators for docker build and run operations
  - Create status updates during long-running operations
  - Add validation for user-selected input folders

## 🧪 Testing Phase 1A
*Complete after each core functionality implementation*

- [ ] **Docker Service Tests**
  - Test docker build with real Dockerfile
  - Test volume mounting and file access
  - Test error handling for docker failures
  - Mock tests for different docker status scenarios

- [ ] **Integration Tests**
  - End-to-end test: drag files → select pipeline → run → get results
  - Test folder selection workflow
  - Test with actual FastQC pipeline
  - Test error scenarios (docker not running, invalid files, etc.)

- [ ] **UI Testing**
  - Manual testing of drag & drop with large files
  - Test folder selection on different OS
  - Verify symlink creation and cleanup
  - Test progress indicators and status updates

## 📚 Documentation Phase 1B
*Complete alongside testing*

- [ ] **Developer Documentation**
  - Update CLAUDE.md with new design decisions
  - Document Docker service architecture and error handling
  - Create troubleshooting guide for common docker issues
  - Add code examples for pipeline development

- [ ] **User Documentation** 
  - Create user guide with screenshots showing both input methods
  - Document supported file types and pipeline requirements
  - Create troubleshooting section for common user issues
  - Add example walkthrough with FastQC pipeline

- [ ] **Pipeline Developer Guide**
  - Create comprehensive guide for creating pipeline packages
  - Document Dockerfile best practices for BioDock
  - Provide template README.md for pipeline authors
  - Add validation checklist for pipeline packages

## 🔧 Phase 2: Enhanced User Experience

### Pipeline Management
- [ ] **Pipeline Library System**
  - Implement local pipeline storage and loading
  - Create pipeline discovery and selection interface
  - Add pipeline metadata display (version, description, requirements)
  - Implement pipeline update and versioning system

- [ ] **Advanced File Support**
  - Extend beyond FASTQ to BAM, FASTA, VCF, etc.
  - Create file type detection and validation
  - Implement file format conversion utilities
  - Add compressed file support (.gz, .bz2)

### Batch Processing
- [ ] **Multi-Sample Support**
  - Design UI for batch sample processing
  - Implement parallel pipeline execution
  - Create batch result management and organization
  - Add batch progress tracking and cancellation

### UI/UX Improvements  
- [ ] **Enhanced Project Management**
  - Improve project creation and organization workflows
  - Add project templates for common analysis types
  - Implement project sharing and export functionality
  - Create project history and audit trail

- [ ] **Results Visualization**
  - Implement integrated report viewer for common outputs (HTML, images)
  - Create result comparison tools for batch analyses
  - Add result export and sharing capabilities
  - Implement basic plotting for numeric results

## 🧪 Testing Phase 2
- [ ] **Expanded Test Coverage**
  - Add tests for new file format support
  - Create performance tests for batch processing
  - Implement UI automation tests for complex workflows
  - Add integration tests with multiple pipeline types

- [ ] **User Acceptance Testing**
  - Create test scenarios based on real bioinformatics workflows
  - Test with actual research data and use cases
  - Gather feedback on UI/UX improvements
  - Document and fix usability issues

## 🚀 Phase 3: Advanced Features  

### Pipeline Creator Enhancement
- [ ] **Visual Pipeline Builder**
  - Complete Pipeline Creator implementation with Dockerfile editing
  - Add pipeline testing and validation tools
  - Create pipeline packaging and export functionality
  - Implement pipeline sharing and distribution tools

### Performance & Scalability
- [ ] **Resource Management**
  - Implement resource monitoring for pipeline execution
  - Add memory and CPU usage tracking
  - Create resource limiting and queue management
  - Optimize for large file handling

### Advanced Analysis Features
- [ ] **Workflow Composition**  
  - Enable chaining multiple pipelines together
  - Create workflow templates for common analysis patterns
  - Implement parameter passing between pipeline steps
  - Add conditional execution and branching logic

## 📋 Continuous Tasks

### Regular Testing & Maintenance
- [ ] **Weekly Testing Schedule**
  - Run full test suite on multiple platforms
  - Test with latest Docker versions
  - Validate pipeline compatibility
  - Performance regression testing

### Documentation Maintenance
- [ ] **Keep Documentation Current**
  - Update README with latest features as they're implemented
  - Maintain developer documentation as architecture evolves
  - Update user guides with new UI features
  - Keep pipeline templates and examples current

### Code Quality
- [ ] **Code Review & Refactoring**
  - Regular code quality reviews
  - Refactor legacy code as new patterns emerge
  - Maintain consistent coding standards
  - Update dependencies and address security vulnerabilities

## 🎯 Success Criteria

### Phase 1 Complete When:
- [x] User can select input via drag/drop files OR folder selection
- [x] Docker pipeline builds and runs successfully
- [x] Results are collected and accessible to user  
- [x] Basic error handling prevents application crashes
- [x] Core functionality is well-tested and documented

### Phase 2 Complete When:
- [x] Multiple file formats are supported beyond FASTQ
- [x] Users can manage a local library of pipelines
- [x] Batch processing handles multiple samples efficiently
- [x] Advanced UI features improve user productivity

### Phase 3 Complete When:
- [x] Pipeline Creator enables custom workflow development
- [x] Advanced features support complex bioinformatics workflows
- [x] Performance supports research-scale data processing
- [x] System is ready for community adoption and contribution

---

## 📝 Notes for Development

**Immediate Next Steps (This Week):**
1. Fix DockerService volume mounting issues
2. Test end-to-end pipeline execution with FastQC
3. Implement basic result collection
4. Create comprehensive error handling

**Key Design Principles to Maintain:**
- Keep UI simple and intuitive for wet lab biologists
- Maintain flexibility for both basic and advanced users  
- Ensure all operations are reversible and safe
- Prioritize data integrity and result reproducibility

**Testing Strategy:**
- Test each phase incrementally before moving to the next
- Always test with real bioinformatics data and workflows
- Include both automated tests and manual user testing
- Document all test scenarios and edge cases

*This TODO will be updated regularly as development progresses and priorities evolve.*