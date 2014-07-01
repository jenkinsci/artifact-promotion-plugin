/**
 * The MIT License
 * Copyright (c) 2014 Halil-Cem Guersoy and all contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.artifactpromotion;

import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

/**
 * A simplistic transfer listener that logs uploads/downloads to the jenkins console.
 * This is based on the sonatype examples for aether but adapted for jenkins to prevent 
 * output garbage.
 */
public class JenkinsConsoleTransferListener
    extends AbstractTransferListener
{
    
    private final static int barWidth = 50;

    private PrintStream jenkinsOut;

    private int lastLength = 0;
    
    public JenkinsConsoleTransferListener( PrintStream jenkinsOut )
    {
        if (jenkinsOut == null) throw new IllegalArgumentException("The transferlistener needs an outputstream. Somtehing has gone wrong");
        this.jenkinsOut = jenkinsOut;
    }

    @Override
    public void transferInitiated( TransferEvent event )
    {
        lastLength = 0;
        String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";
        jenkinsOut.println( message + ": " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName() );
    }

    @Override
    public void transferProgressed( TransferEvent event )
    {
        TransferResource resource = event.getResource();
        long total = resource.getContentLength();
        long complete = event.getTransferredBytes();
        long percent = (long)(((double)complete / (double)total) * 100);
        int length = (int)(barWidth * percent / 100);
        if (length > lastLength) {
            StringBuffer bar = new StringBuffer(length - lastLength);
            for (int i = 0; i < length - lastLength; i++) {
                bar.append("#");
            }
            jenkinsOut.print(bar.toString());                
            lastLength = length;
        }
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        transferCompleted( event );
        TransferResource resource = event.getResource();
        long contentLength = event.getTransferredBytes();
        if ( contentLength >= 0 )
        {
            String type = ( event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded" );
            String throughput = "";
            long duration = System.currentTimeMillis() - resource.getTransferStartTime();
            if ( duration > 0 )
            {
                long bytes = contentLength - resource.getResumeOffset();
                DecimalFormat format = new DecimalFormat( "0.0", new DecimalFormatSymbols( Locale.ENGLISH ) );
                double kbPerSec = ( bytes / 1024.0 ) / ( duration / 1000.0 );
                throughput = " at " + format.format( kbPerSec ) + " KB/sec";
            }
            jenkinsOut.println( type + ": " + resource.getRepositoryUrl() + resource.getResourceName() + " (" + throughput + ")" );
        }
    }

    @Override
    public void transferFailed( TransferEvent event )
    {        
        jenkinsOut.println("\rSomething has gone wrong and the transfer has failed: " + event.getException().getMessage());
    }

    private void transferCompleted( TransferEvent event )
    {
        jenkinsOut.println("]");
    }

    public void transferCorrupted( TransferEvent event )
    {
        jenkinsOut.println("\rSomething has gone wrong and the transfer has been corrupted: " + event.getException().getMessage());
    }
}