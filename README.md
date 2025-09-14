# BioDock 🧬
*A standardized platform for containerized bioinformatics pipeline management.*

## 🌟 Overview
BioDock is a desktop application that revolutionizes how bioinformatics workflows are shared and executed. Built around a "pipeline-as-a-package" philosophy, BioDock enables researchers to package their workflows as standardized Docker containers with JSON configuration files, while providing wet lab biologists with an intuitive drag-and-drop interface to execute these pipelines without dealing with dependencies, environment setup, or command-line complexity.

### The BioDock Approach
- **For Pipeline Authors**: Package your workflow as a Dockerfile + JSON configuration and share it with the community
- **For End Users**: Simply drag-and-drop your input files, select a pipeline, and get results - BioDock handles everything else
- **For Everyone**: Reproducible, containerized execution ensures consistent results across different systems


## 🚀 Features

### For End Users
- **Drag-and-Drop Interface** – Load input files and execute pipelines instantly
- **Standardized Pipeline Library** – Access validated, containerized workflows 
- **Project Management** – Organize samples, track analysis history, and manage results
- **Built-in Report Viewer** – View analysis results and reports directly in the app
- **Cross-Platform** – Runs on Windows, macOS, and Linux with Docker

### For Pipeline Developers
- **Pipeline Creator Interface** – Visual tool for defining Dockerfile and JSON configurations
- **Standardized Package Format** – Consistent structure for sharing and distributing workflows
- **Container Integration** – Built-in Docker support for dependency management
- **Validation Tools** – Ensure pipeline packages meet quality standards before sharing

### Core Capabilities  
- **Reproducible Execution** – Docker containers ensure consistent results across environments
- **Multi-Format Support** – Handle various bioinformatics file types (FASTQ, FASTA, BAM, etc.)
- **Provenance Tracking** – Complete audit trail of tool versions, commands, and execution details
- **Metadata Management** – Integrated support for sample sheets and experimental metadata


## 📸 UI Preview
*(screenshot placeholder here)*

- **Left sidebar** – project samples & runs
- **Center tabs** – Logs | Results | Reports | Provenance
- **Top bar** – Pipeline selector & Run button
- **Bottom bar** – progress + Docker status


## 🔧 Installation
1. Install [Docker](https://www.docker.com/).
2. Download the BioDock installer from [Releases](https://github.com/antonio-dimeglio/BioDock/releases).
3. Launch the app – everything else is handled automatically.

## 🧪 Quick Start

### Running Your First Analysis
1. **Install BioDock** and ensure Docker is running
2. **Create a New Project** or open an existing one
3. **Provide Input Data** (choose your preferred method):
   - **Option A**: Drag & drop individual files - BioDock creates a temporary folder with symlinks (no file copying!)
   - **Option B**: Select an input folder containing all your files (great for organized datasets or remote storage)
4. **Select Output Folder** - Choose where you want results to be saved
5. **Select a Pipeline** from the available library (e.g., FastQC Quality Control)
6. **Click Run** - BioDock will build the Docker image and execute your pipeline
7. **View Results** in your specified output folder

**Key Benefits**:
- **No File Duplication**: Drag & drop uses symlinks to avoid copying large genomics files
- **Remote Storage Support**: Folder selection works seamlessly with mounted network drives
- **Flexible Workflow**: Use whatever input method works best for your data organization

### Creating Your First Pipeline Package

#### For Pipeline Developers
1. **Create a Dockerfile** that installs your tools and sets `WORKDIR /data/`
2. **Create a Pipeline JSON** configuration file (see example below)
3. **Create a README.md** explaining to end users:
   - What file types your pipeline expects
   - How files should be organized in the input folder
   - What outputs will be generated
   - Any special requirements or usage notes
4. **Package Everything** in a folder structure as shown below

#### Pipeline Package Structure
```
pipelines/
└── fastqc-v0.12.1/
    ├── Dockerfile          # Your containerized tools
    ├── pipeline.json       # Pipeline configuration
    └── README.md          # User instructions (REQUIRED)
```

#### Example Pipeline JSON Structure
```json
{
  "id": "fastqc-v0.12.1",
  "name": "FastQC Quality Control",
  "description": "Quality control checks on raw sequence data",
  "version": "0.12.1",
  "command": ["fastqc", "--outdir", "/results", "/data/*"],
  "inputFileTypes": ["fastq", "fq", "fastq.gz"]
}
```

#### Example Pipeline README.md
Every pipeline should include a README.md file explaining usage:

```markdown
# FastQC Quality Control Pipeline

## Input Requirements
- **File Types**: FASTQ files (.fastq, .fq, .fastq.gz)
- **Organization**: Place all FASTQ files you want to analyze in your input folder
- **File Naming**: No specific naming requirements - FastQC will process all FASTQ files

## Usage
1. Organize your FASTQ files in a folder (or drag & drop them into BioDock)
2. Select this pipeline
3. Choose an output folder
4. Click Run

## Output
- HTML quality reports for each input file
- ZIP archives containing detailed analysis data
- All outputs will be saved to your chosen output folder
```

**Key Points for Pipeline Authors**:
- Commands should expect input files at `/data/`
- Output should go to `/results/` (BioDock handles mounting)
- **Always include a README.md** - this is essential for user experience
- Use `inputFileTypes` to help BioDock validate user files
- Design pipelines to be flexible with file organization when possible

## 🗺️ Roadmap

### Current Implementation (v0.x)
- [x] Core JavaFX application architecture
- [x] Project management and drag-and-drop file loading
- [x] Pipeline data model with JSON serialization
- [x] Basic FastQC pipeline configuration
- [x] Pipeline Creator interface scaffolding

### Version 1.0 Goals
- [ ] **Complete Docker Service** - Full container management and execution
- [ ] **Pipeline Validation Engine** - Automated testing of pipeline packages
- [ ] **Result Processing System** - Report generation and visualization
- [ ] **Multiple Sample Support** - Batch processing capabilities
- [ ] **Pipeline Library Management** - Local pipeline storage and organization
- [ ] **Advanced File Format Support** - Beyond FASTQ (BAM, FASTA, etc.)

### Future Vision (Post v1.0)
- [ ] **Centralized Pipeline Repository** - Community-driven pipeline sharing platform
- [ ] **Advanced Search & Discovery** - Browse pipelines by category, rating, usage
- [ ] **One-Click Pipeline Installation** - Direct download from central repository
- [ ] **Community Features** - Pipeline ratings, reviews, and collaboration tools
- [ ] **Quality Assurance System** - Automated validation and testing of community contributions
- [ ] **Version Management** - Semantic versioning with dependency tracking

### Long-term Impact
Transform BioDock into a comprehensive ecosystem for bioinformatics workflow management, similar to how Docker Hub revolutionized container sharing but specifically tailored for computational biology research.

## 🤝 Contributing

BioDock is in active development and welcomes contributions from the bioinformatics community! Whether you're interested in:

- **Core Application Development** - JavaFX/Kotlin development, Docker integration
- **Pipeline Development** - Creating standardized pipeline packages for common workflows  
- **Documentation** - Improving user guides, API documentation, or examples
- **Testing** - Validating pipeline packages or application functionality
- **UI/UX Design** - Enhancing the user experience for researchers

### How to Contribute
1. **Fork the Repository** and create a feature branch
2. **Follow the Development Guidelines** outlined in [CLAUDE.md](CLAUDE.md)
3. **Test Your Changes** using the provided Maven commands
4. **Submit a Pull Request** with a clear description of your changes

### Pipeline Contributors
If you have bioinformatics workflows you'd like to share:
1. Use the Pipeline Creator interface to package your workflow
2. Test your pipeline with sample data
3. Submit your pipeline package for inclusion in the standard library
4. Help us build the foundation for the future centralized repository!

Please open an [issue](https://github.com/antonio-dimeglio/BioDock/issues) for bug reports, feature requests, or general discussion.

## 📜 License
MIT License – see [LICENSE](LICENSE) for details.