# bdrc-libraries
Repo for common code libraries to be used across tools such as xmltoldmigration, git-to-dbs, and editserv

### Maven dependency
    <dependency>
      <groupId>io.bdrc.libraries</groupId>
      <artifactId>bdrc-libraries</artifactId>
      <version>0.15.0</version>
    </dependency>

### Some release info
Basic mvn:

First time in a fresh clone:

    git submodule update --init

later:

    git submodule update --recursive --remote
    
build and deploy:

    mvn clean package
    mvn deploy -DperformRelease=true

Then go to `https://oss.sonatype.org/`  and do the *close* and *release*
