# BioDock 🧬
*A click-and-run bioinformatics pipeline runner for wet lab biologists.*

---

## 🌟 Overview
BioDock is a desktop application that lets you run common bioinformatics workflows
without needing to touch the command line. Designed for wet lab researchers who
want quick quality control and analysis of sequencing data, BioDock wraps standard
tools (e.g., FastQC, aligners, quantifiers) inside an easy-to-use graphical interface.

---

## 🚀 Features
- **One-click analysis** – load FASTQ files and start pipelines instantly.
- **Pipeline presets** – QC-only, RNA-seq quick analysis, variant calling, and more.
- **Project management** – organize multiple samples and track run history.
- **Report viewer** – inspect FastQC or pipeline reports inside the app.
- **Provenance tracking** – every run logs tool versions, container hashes, and commands.
- **Metadata support** – import sample sheets (CSV/Excel) for structured experiments.
- **Cross-platform** – runs on Windows, macOS, and Linux with Docker.

---

## 📸 UI Preview
*(screenshot placeholder here)*

- **Left sidebar** – project samples & runs
- **Center tabs** – Logs | Results | Reports | Provenance
- **Top bar** – Pipeline selector & Run button
- **Bottom bar** – progress + Docker status

---

## 🔧 Installation
1. Install [Docker](https://www.docker.com/).
2. Download the BioDock installer from [Releases](link-to-your-releases).
3. Launch the app – everything else is handled automatically.

---

## 🧪 Quick Start
1. Open BioDock.
2. Drag & drop your FASTQ files.
3. Select a pipeline preset (e.g., **FastQC only**).
4. Hit **Run**.
5. View reports directly in the app.

---

## 🗺️ Roadmap
- [x] Run FastQC on single samples
- [ ] Multiple-sample project support
- [ ] Pipeline presets (RNA-seq, Variant calling)
- [ ] Provenance export (JSON/HTML)
- [ ] Metadata editor (sample sheets)
- [ ] Summary plots & PDF report export

---

## 🤝 Contributing
BioDock is in early development. Contributions and feedback are welcome!
Please open an [issue](link-to-issues) or submit a pull request.

---

## 📜 License
MIT License – see [LICENSE](LICENSE) for details.
