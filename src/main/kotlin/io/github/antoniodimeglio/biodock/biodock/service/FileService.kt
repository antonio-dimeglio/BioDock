package io.github.antoniodimeglio.biodock.biodock.service

import java.io.File

class FileService {
//    fun validateFastqFile(file: File): ValidationResult {
//
//    }
//
//    fun extractSampleNameFromFilename(file: File): String {
//
//    }
//
//    fun cleanupTempFiles(directory: File){
//
//    }


}

sealed class ValidationResult {
    data class Success(val message: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}