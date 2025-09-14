package io.github.antoniodimeglio.biodock.biodock.util

object StringGenerators {
     fun generateSyntheticFastq(): String {
        val sequences = listOf(
            "ATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCGATCG",
            "GCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCTAGCT",
            "TTAAGGCCTTAAGGCCTTAAGGCCTTAAGGCCTTAAGGCCTTAAGGCCTTAAGGCC",
            "CCGGAATTCCGGAATTCCGGAATTCCGGAATTCCGGAATTCCGGAATTCCGGAATT"
        )

        val fastqContent = StringBuilder()

        // Generate multiple reads for meaningful FastQC analysis
        repeat(1000) { readIndex ->
            val sequence = sequences[readIndex % sequences.size]
            val qualityScores = "I".repeat(sequence.length) // High quality scores

            fastqContent.appendLine("@read_$readIndex")
            fastqContent.appendLine(sequence)
            fastqContent.appendLine("+")
            fastqContent.appendLine(qualityScores)
        }

        return fastqContent.toString()
    }
}