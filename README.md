# bdrc-libraries
Repo for common code libraries to be used across tools such as xmltoldmigration, git-to-dbs, and editserv

Current version: 0.2.0

### Maven dependency
    <dependency>
      <groupId>io.bdrc.libraries</groupId>
      <artifactId>bdrc-libraries</artifactId>
      <version>0.1.0</version>
    </dependency>

### Some release info
Basic mvn:

    mvn clean package
    mvn deploy -DperformRelease=true

Then go to `https://oss.sonatype.org/`  and do the *close* and *release*
