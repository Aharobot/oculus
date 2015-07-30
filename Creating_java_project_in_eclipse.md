**Creating new eclipse project from svn:**
  1. download OCULUS FULL archive, unpack
  1. make backup copies of Oculus/webapps/oculus/flash & images folders and their contents, you'll need to recopy them back later
  1. delete Oculus/webapps/oculus folder and contents
  1. open eclipse (with subclipse plugin)
  1. set working directory to Oculus/webapps
  1. set svn repository to https://oculus.googlecode.com/svn/trunk/
    * select the 'oculus' branch
  1. checkout /trunk/oculus as new project 'oculus'
  1. under project properties > java build path:
    * remove src folder created by default
    * add WEB-INF/src as src folder
    * change default output folder to oculus/WEB-INF/classes
    * add Jars > select all under /oculus/WEB-INF/lib > add
    * add External Jars:  red5.jar (**NOTE** you have link to a COPY of red5.jar, so no conflicts with red5 core operation)
  1. Change project setup, add JUnit4 library to build path
  1. copy flash and images folders and their contents (from backup created in step 2) to Oculus/
  1. make sure Oculus/webapps/.metadata folder is hidden or red5 will try to run it (windows)


**Check settings**

![http://oculus.googlecode.com/files/libraries_path.png](http://oculus.googlecode.com/files/libraries_path.png)

![http://oculus.googlecode.com/files/src_path.png](http://oculus.googlecode.com/files/src_path.png)