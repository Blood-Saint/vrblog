package com.vasilrem.hudson.ftpcourier
import org.apache.commons.net.ftp.FTPClient

///////////////////////////////////////////////////////
/// PLEASE ASSIGN CORRECT VALUES TO VARIABLES BELOW ///
def FTP            = ''
def FTP_USER       = ''
def FTP_PASS       = ''
def FTP_LOCATION   = ''
def HUDSON_HOME    = ''
def PROJECT_NAME   = ''
///////////////////////////////////////////////////////

putArtifactToFTP(findWARArtefact(HUDSON_HOME + '/jobs/' + PROJECT_NAME + '/builds', []),
    getBuildNumber(HUDSON_HOME + '/jobs/' + PROJECT_NAME + '/nextBuildNumber'),
    connectToFTP(FTP, FTP_USER, FTP_PASS, FTP_LOCATION))

def connectToFTP(ftpHost, ftpUserName, ftpUserPass, ftpLocation){
    def ftpClient = new FTPClient()
    ftpClient.connect(ftpHost)
    ftpClient.enterLocalPassiveMode()
    ftpClient.login(ftpUserName,ftpUserPass)
    ftpClient.changeWorkingDirectory(ftpLocation)
    ftpClient.fileType=(FTPClient.BINARY_FILE_TYPE)
    println 'Connected to FTP!'
    ftpClient
}

def findWARArtefact(fileName, results){
    def file = new File(fileName)
    if(file.isDirectory()){
        file.eachFile{
            f->            
            findWARArtefact(fileName + '/' + f.name, results)
        }
    }else{
        if(fileName.endsWith('.war')){
            println 'Found WAR: ' + fileName
            results.add(fileName)
        }
        return
    }
    new File(results[0])
}

def getBuildNumber(buildNumFileName){
    def buildNumFile = new File(buildNumFileName)
    def buildNum = '0'
    buildNumFile.eachLine{
        ln-> if(ln!= null){
            buildNum = new Integer(ln) - 1
        }
    }
    buildNum
}

def putArtifactToFTP(warArtifact, buildNumber, ftpClient){
    if(warArtifact != null){
        acrtifactName = 'build.'+ buildNumber + '.' + warArtifact.name
        println 'Copying ' + acrtifactName + ' to FTP...'
        warArtifact.withInputStream { istream ->
            ftpClient.storeFile(acrtifactName, istream )
        }
        println(ftpClient.replyString);
        ftpClient.disconnect()
        println "File is copied!"
    }else{
        println 'Artifact not found!'
    }
}

    