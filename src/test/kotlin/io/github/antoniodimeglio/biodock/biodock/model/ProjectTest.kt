package io.github.antoniodimeglio.biodock.biodock.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class ProjectTest {
    fun createSample(status: SampleStatus) = Sample(
        name = "test-sample",
        file = File(""),
        status = status
    )

    @Test
    fun `getOverallStatus should return Empty for project with no samples`() {
        // Business rule: Empty sample lists should return Empty

        // Given: project with no samples
        // When: getOverallStatus called
        // Then: returns "Empty"
        val project = Project(name="EmptyProject")
        val expected = "Empty"
        val result = project.getOverallStatus()

        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should return SomeFailed when mixed with failed samples`() {
        // Business rule: Any failed samples should show "Some Failed"
        // even if pending samples exist

        // Given: project with both failed and pending samples
        // When: getOverallStatus called
        // Then: returns "Some Failed"
        val project = Project(name="failedProject",
            samples=mutableListOf(
                createSample(SampleStatus.FAILED),
                createSample(SampleStatus.FAILED),
                createSample(SampleStatus.COMPLETED),
            ))
        val expected = "Some Failed"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should return Running when any sample is running and all others are complete`() {
        // Business rule: Any running samples should return "Running"
        // when all but one are complete

        // Given: project with all but one sample running
        // When: getOverallStatus called
        // Then: returns "Running"
        val project = Project(name="runningProject",
            samples=mutableListOf(
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.RUNNING),
            ))
        val expected = "Running"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should return Pending when any sample is pending and all others are complete`() {
        // Business rule: Any pending samples should return "Running"
        // when all but one are complete

        // Given: project with all but one sample pending
        // When: getOverallStatus called
        // Then: returns "Pending"
        val project = Project(name="pendingProject",
            samples=mutableListOf(
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.PENDING),
            ))

        val expected = "Pending"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should return Complete when all samples are completed`() {
        // Business rule: If all samples are complete the overall status should be complete

        // Given: project with all samples complete
        // When: getOverallStatus called
        // Then: returns "Complete"
        val project = Project(name="completeProject",
            samples=mutableListOf(
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.COMPLETED),
                createSample(SampleStatus.COMPLETED),
            ))

        val expected = "Complete"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should return cancelled when all samples are cancelled`() {
        // Business rule: If all samples are cancelled the function should return "Cancelled"

        // Given: project with only cancelled samples
        // When: getOverallStatus called
        // Then: does return "Cancelled"
        val project = Project(name="completeProject",
            samples=mutableListOf(
                createSample(SampleStatus.CANCELLED),
                createSample(SampleStatus.CANCELLED),
                createSample(SampleStatus.CANCELLED),
            ))

        val expected = "Cancelled"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }

    @Test
    fun `getOverallStatus should ignore cancelled samples when other types are present`() {
        // Business rule: If cancelled samples are present
        // but also non-cancelled ones are present the status should not be Cancelled

        // Given: project with cancelled samples but also non-cancelled ones
        // When: getOverallStatus called
        // Then: does not return "Cancelled"
        val project = Project(name="failedProject",
            samples=mutableListOf(
                createSample(SampleStatus.CANCELLED),
                createSample(SampleStatus.CANCELLED),
                createSample(SampleStatus.FAILED),
            ))

        val expected = "Some Failed"
        val result = project.getOverallStatus()
        assertEquals(expected, result)
    }
}