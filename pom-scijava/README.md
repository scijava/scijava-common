The pom-scijava project is a Maven POM that serves as the base for all
Maven-based SciJava code, including:

* [Fiji](https://github.com/fiji/fiji)
* [Image5D](https://github.com/imagej/image5d)
* [ImageJ1 unit tests](https://github.com/imagej/ij1-tests)
* [ImageJ2](https://github.com/imagej/imagej)
* [ImageJA](https://github.com/fiji/imageja)
* [ImgLib](https://github.com/imglib/imglib)
* [LOCI](https://github.com/uw-loci/pom-loci)
* [SCIFIO](https://github.com/imagej/minimal-ij1-plugin)
* [SciJava Common](https://github.com/scijava/scijava-common)

This POM is intended for use as the parent of your own Maven-based code.
See these examples for guidance:

* [ImageJ1 plugin template](https://github.com/imagej/minimal-ij1-plugin)
* [ImageJ2 tutorials](http://github.com/imagej/imagej-tutorials)

For more information about Maven, see:

* [Learning Maven](http://developer.imagej.net/learning-maven)
* [Maven FAQ](http://wiki.imagej.net/Maven_-_Frequently_Asked_Questions)

Adding a new project to pom-scijava
===================================

Including a project in pom-scijava is very beneficial. It means that a
given pom-scijava version will be coupled with a specific version of
that project, and by using the project.version properties, all
consumers of pom-scijava will use only that version of the project.

It also minimizes bookkeeping, as whenever we run the Jenkins
Bump-POM-SciJava job, the version property for each project is
automatically updated to the latest release. By having a version property,
there is no need to go through downstream projects updating their
dependencies - a process that only invites mistakes.

There are several steps to include a project in pom-scijava, but they
only need to be done once. The following tutorial will guide you through
the process.

Preface
-------

This tutorial assumes the project is a github project.

We will use these properties throughout the tutorial:

* PROPERTY
* GROUPID
* ARTIFACTID
* VERSION
* URL
* KEY_NAME

The GROUPID and ARTIFACTID come directly from your project's pom.xml.

VERSION refers to the latest deployed version, and is the first version
that will be included in pom-scijava (e.g. the only time you will manually set the version).

PROPERTY is the prefix for your project's properties. For example, SciJava-common
uses the property "scijava-common". Thus when referencing SciJava-common properties via
pom-scijava, you can use "scijava-common.version" or "scijava-common.groupId". This value
can be anything you choose, but should be appropriate and unique to your project, to avoid
clashing with other properties.

URL is the github url of your project.

KEY_NAME is the short name you give to your deploy key (see step 2).

1 - Create a deploy key
-----------------

First, as the jenkins user on dev.loci.wisc.edu, run:

  ```
  ~/bin/add-github-deploy-key.sh KEY_NAME
  ```

to generate a SSH public/private key pair. Copy the public key fingerprint from
this script's output for use later.

You will need to add this key in github to allow Jenkins access to your
project. See the [github help
page](https://help.github.com/articles/managing-deploy-keys) for instructions
on setting up a deploy key. Use the following settings for your deploy key:

* Title: Jenkins@dev.imagejdev.org
* Key: paste the public key fingerprint you copied earlier

Click "Add key" and go to the next step!

2 - Update Pom-SciJava
----------------------

First make sure your SciJava-common is up to date.

In pom-scijava/pom.xml :

* Manually increase the minor ```<version>``` of the pom - e.g. ```1.32``` goes to ```1.33```
* Find the ```<properties>``` block:
  * Add a comment block defining your project, e.g.
      ```
      <!-- project name - URL -->
      ```
  * If your project's groupId is not already defined, add an entry of the form:
    ```
    <PROPERTY.groupId>GROUPID</PROPERTY.groupId>
    ```
  * Add a version entry of the form:
    ```
    <PROPERTY.version>VERSION</PROPERTY.version>
    ```

Save and commit your changes.

3 - Update bump-pom-scijava.sh
------------------------------

Again in the SciJava-common repository, edit the file:

bin/bump.pom-scijava.sh

There are two changes to make.

* First, search for:

    ```
    test "a--default-properties"
    ```

  In the following "set" block, add the line:

   ```
   PROPERTY.version --latest
   ```

  NB: Make sure all lines except the last of the set block are terminated with a backslash ('\') character.

* Second, search for:

    ```
    if test "a--latest" = "a$value"
    ```

  In the following "then" block, add the lines:

    ```
    PROPERTY.version)
       ga=GROUPID:ARTIFACTID
       ;;
    ```

  To ensure the bump-pom-scijava script sets the gav correctly for your project.

Save and commit your changes.

4 - Update Jenkins
------------------

You will need someone to configure the [Bump-POM-SciJava](http://jenkins.imagej.net/view/SciJava/job/Bump-POM-SciJava/) job
for you, if you do not have the rights to do so yourself.

Two changes need to be added here:

* First, in the Parameter section, use "Add Parameter -> Boolean Value". Name it "UPDATE_PROJECT" as appropriate for your
project.

* Second, in the Build section under "Execute shell", search for:

    ```
    CHILDREN=
    ```

  At the end of this block, add these two lines:

    ```
    test true != "$UPDATE_PROJECT" ||
    CHILDREN="$CHILDREN KEY_NAME:URL"
    ```

That's it! The next time Bump-POM-SciJava is run, there will be a check box for
your project (however you named the UPDATE_PROJECT variable). Regardless if
this box is checked or not, the base pom-scijava version for your project will
be updated to the latest released. If the box is checked for a given project,
then that project's parent pom will also be updated to the latest pom-scijava
version.



