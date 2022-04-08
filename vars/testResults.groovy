import hudson.model.*
import jenkins.model.*
import hudson.tasks.test.AbstractTestResultAction
import java.text.DecimalFormat;
import java.time.*
import java.time.format.DateTimeFormatter;


def emailTestReport = ""
def totalNumberOfTests = 0
def passedNumberOfTests = 0
def failedNumberOfTests = 0
def skippedNumberOfTests = 0
String failedPercent = ""
String skippedPercent = ""
String passedPercent = ""
def failedDiff
def readableDuration
String TotalTime = ""
String startTime;
String completionTime;
def authors
def notificationMessage

def call(){
  return buildNotificationMessage()
}

def buildNotificationMessage() {
    AbstractTestResultAction testResultAction = currentBuild.rawBuild.getAction(AbstractTestResultAction.class)
    def build = currentBuild

    long startMillis = currentBuild.getStartTimeInMillis();
    // int durationMillis = build.duration;

    startTime = Instant.ofEpochMilli(startMillis).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MMM hh:mm:ss a"));

    // long finishMillis = startMillis + durationMillis;
    long finishMillis = currentBuild.getTimeInMillis();
    completionTime = Instant.ofEpochMilli(finishMillis).atZone(ZoneId.systemDefault()).toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MMM hh:mm:ss a"));

    // Duration duration = Duration.between(Instant.ofEpochMilli(startMillis), Instant.ofEpochMilli(finishMillis));
    // readableDuration = duration.toString().substring(2).replaceAll(~'(\\d[HMS])(?!$)', '$1 ').toLowerCase();
    readableDuration = build.getDurationString();
    TotalTime = readableDuration.replace('and counting', '');
    /*authors = currentBuild.changeSets.collectMany { it.toList().collect { it.author } }.unique()*/

    if (testResultAction != null) {
        DecimalFormat df = new DecimalFormat("##.##%");
        totalNumberOfTests = testResultAction.totalCount
        failedNumberOfTests = testResultAction.failCount
        failedDiff = testResultAction.failureDiffString
        skippedNumberOfTests = testResultAction.skipCount
        passedNumberOfTests = totalNumberOfTests - failedNumberOfTests - skippedNumberOfTests
        passedPercent = df.format(passedNumberOfTests / totalNumberOfTests)
        failedPercent = df.format(failedNumberOfTests / totalNumberOfTests)
        skippedPercent = df.format(skippedNumberOfTests / totalNumberOfTests)
        emailTestReport = "Tests Report: Passed: ${passedNumberOfTests}; Failed: ${failedNumberOfTests} ${failedDiff}; Skipped: ${skippedNumberOfTests}  out of ${totalNumberOfTests} "
    }
    def map = [TotalTestCase: """${totalNumberOfTests}""", StartTime: """${startTime}""", EndTime: """${completionTime}""", PassingPercentage: """${passedNumberOfTests}/${passedPercent}""", Passed: """"${passedNumberOfTests}""", Failed: """${failedNumberOfTests}/${failedPercent}""", Duration: """${TotalTime}"""]
    return map;
}
