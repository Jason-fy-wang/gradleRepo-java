package org.example.app.dsl

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import org.apache.commons.io.output.CloseShieldOutputStream
import org.apache.commons.io.output.TeeOutputStream

import java.util.regex.Pattern

class CommandOutput {
    int exitStatus
    String output
    Throwable exception
}

class RemoteSessionData {
    static final int DEFAULT_SSH_PORT = 22
    static final Pattern SSH_URL =  ~/^(([^:@]+):([^@]+)?@)?([^:]+)(:([0-9]+))?$/

    String host = null
    int port = DEFAULT_SSH_PORT
    String username = null
    String password = null

    def setUrl(String url){
        def matcher = SSH_URL.matcher(url)
        if (matcher.matches()) {
            username = matcher.group(2)
            password = matcher.group(3)
            host = matcher.group(4)
            port = matcher.group(6) ? matcher.group(6).toInteger() : DEFAULT_SSH_PORT
        }else {
            throw new RuntimeException("unknown url format: $url")
        }
    }
}


class RemoteSession extends RemoteSessionData {
    Session session = null

    JSch jsch = null

    RemoteSession(JSch jsch){
        this.jsch = jsch
    }

    def connect() {
        if (session == null || !session.connected) {
            disconnect()
        }

        if(host == null) {
            throw new RuntimeException("host is not set")
        }

        session = jsch.getSession(username, host,port)
        session.password = password
        println ">>> Connecting to $host:$port as $username"
        session.connect(20000)
    }

    def disconnect() {
        if (session != null && session.connected) {
            println ">>> Disconnecting from $host:$port"
            try {
                session.disconnect()
            }catch (Exception e) {

            }finally {
                println "<<< Disonnected from $host"
            }
        }
    }

    def reconnect() {
        disconnect()
        connect()
    }

    CommandOutput exec(String cmd) {
        connect()

        catchExceptionClosure {
            awaitTermination(executeCommand(cmd))
        }
    }

    ChannelData executeCommand(String cmd) {
        println "> cmd $cmd"

        def channel = session.openChannel("exec") as ChannelExec
        def saveOutput = new ByteArrayOutputStream()

        def systemOutput = new CloseShieldOutputStream(System.out)

        def output = new TeeOutputStream(saveOutput, systemOutput)

        channel.setCommand(cmd)
        channel.outputStream = output
        channel.extOutputStream = output
        channel.setPty(true)
        channel.connect()

        new ChannelData(channel:channel, output:saveOutput)
    }


    class ChannelData {
        ByteArrayOutputStream output
        Channel channel
    }

    CommandOutput awaitTermination(ChannelData data) {
        def channel = data.channel
        try {
            def thread = null
            thread = Thread.start {
               while (!channel.isClosed()) {
                   if (thread==null) {
                       return
                   }
                     Thread.sleep(1000)
               }
            }


            thread.join()
            if (thread.isAlive()) {
                thread == null
                return failWithTimeout()
            }else{
                def status = channel.exitStatus
                return new CommandOutput(exitStatus:status, output:data.output.toString())
            }

        }catch (Exception e) {

        }finally {
            channel.disconnect()
        }

    }


    CommandOutput catchExceptionClosure(Closure cl) {
        try {
            return cl()
        }catch (Exception e) {
            return failWithException(e)
        }
    }

    CommandOutput failWithTimeout(){
        println "Session Timeout"

        new CommandOutput(exitStatus:-1, output:"Session Timeout")
    }

    CommandOutput failWithException(Exception e) {
        println "Exception: $e"

        new CommandOutput(exitStatus:-1, output:"Exception: $e")
    }
}



class SSHEngine {
    JSch jSch
    RemoteSession  delegate

    SSHEngine() {
        JSch.setConfig("HashKnownHosts", "no")
        JSch.setConfig("StrictHostKeyChecking", "no")
        this.jSch = new JSch()
    }

    def remoteSession(Closure cl) {
        if (cl != null) {
            delegate = new RemoteSession(jSch)

            cl.delegate = delegate
            cl.resolveStrategy = Closure.DELEGATE_FIRST
            cl()
            if(delegate?.session?.connected) {
                try {
                    delegate.session.disconnect()
                }catch (Exception e){
                    println(e)
                }
            }
        }
    }
}

new SSHEngine().remoteSession {
    url = "scott:loongson@192.168.20.21:22"
    exec("ls -lh /")
}
