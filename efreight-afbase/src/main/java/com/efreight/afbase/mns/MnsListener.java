package com.efreight.afbase.mns;

import job.JobControl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class MnsListener extends Thread implements ServletContextListener {

    public void contextDestroyed(ServletContextEvent arg0)
    {
        System.out.println("unload mns==========================");
        JobControl.shunDown();
    }

    public void contextInitialized(ServletContextEvent arg0)
    {
        System.out.println("load mns=========================");
        super.start();
    }

    public void run()
    {
        try
        {
            Thread.sleep(30000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        JobControl.startJob();
    }
}
