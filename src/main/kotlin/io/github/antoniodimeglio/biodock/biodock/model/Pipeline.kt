package io.github.antoniodimeglio.biodock.biodock.model

data class Pipeline(
    val id: String,
    val name: String,
    val description: String,
    val dockerImage: String,
    val command: List<String>,
    val inputFileTypes: List<String> = listOf("fastq", "fq"),
    val outputFileTypes: List<String> = listOf("html", "txt", "zip"),
    val estimatedDuration: String = "5-10 minutes",
    val requiredMemory: String = "2GB",
    val version: String = "latest"
) {
    companion object {
        fun getDefaultPipelines(): List<Pipeline> = listOf(
            Pipeline(
                id = "fastqc-only",
                name = "FastQC Only",
                description = "Quality control analysis of raw sequence data",
                dockerImage = "staphb/fastqc:latest",
                command = listOf("fastqc", "--outdir=/output", "/input/{filename}"),
                estimatedDuration = "2-5 minutes",
                requiredMemory = "1GB"
            ),
            Pipeline(
                id = "rnaseq-qc",
                name = "RNA-seq Quick QC",
                description = "FastQC + basic RNA-seq quality metrics",
                dockerImage = "biodock/rnaseq-qc:latest",
                command = listOf("rnaseq_qc.sh", "/input/{filename}", "/output"),
                estimatedDuration = "10-15 minutes",
                requiredMemory = "4GB"
            ),
            Pipeline(
                id = "variant-qc",
                name = "Variant Calling QC",
                description = "Quality control for variant calling workflows",
                dockerImage = "biodock/variant-qc:latest",
                command = listOf("variant_qc.sh", "/input/{filename}", "/output"),
                estimatedDuration = "15-30 minutes",
                requiredMemory = "8GB"
            )
        )
    }
}