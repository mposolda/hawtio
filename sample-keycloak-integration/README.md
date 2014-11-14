Hawtio integration with Keycloak
================================

Enable integration on Karaf or JBoss Fuse
-----------------------------------------
This was tested with Apache Karaf 2.3.9 and JBoss Fuse 6.1.0-redhat379 . 

1) First checkout latest keycloak and build from sources
        
```shell
git clone https://github.com/keycloak/keycloak.git
cd $BASE_DIR/keycloak
mvn clean install
````

2) Checkout latest hawtio from 'mposolda' where is Keycloak integration enabled. Use branch 'hawtio-keycloak' :

```shell
cd $BASE_DIR
git clone https://github.com/mposolda/hawtio.git
cd hawtio
git checkout origin/hawtio-keycloak
mvn clean install 
````

3) Run keycloak server on localhost:8081 and import demo realm into it.

```shell
cd $BASE_DIR/keycloak/testsuite/integration
mvn exec:java -Pkeycloak-server -Dkeycloak.import=$BASE_DIR/hawtio/sample-keycloak-integration/demorealm.json
````

Realm has 'hawtio-client' application installed as public client. It has also application 'direct-auth-fuse-client' for non-web services like SSH,
which need to directly access keycloak based on username/password. There are 2 realm roles 'admin' and 'viewer' . Names of these roles are same like 
default hawtio roles, which are allowed to login into hawtio admin console.

There are also 3 users:

root --- with password 'password' and role 'admin', so he is allowed to login into hawtio

john --- with password 'password' and role 'viewer', so he is allowed to login into hawtio

mary --- with password 'password' and no role assigned, so she is not allowed to login into hawtio


Now steps specific for karaf and JBoss Fuse. They are almost same on both. Assuming $FUSE_HOME is the root directory of your fuse/karaf

4) Add this into the end of file $FUSE_HOME/etc/system.properties :

```shell 
hawtio.keycloakEnabled=true
hawtio.realm=keycloak
hawtio.rolePrincipalClasses=org.keycloak.adapters.jaas.RolePrincipal
hawtio.keycloakClientConfig=${karaf.base}/etc/keycloak-hawtio.json
hawtio.keycloakServerConfig=${karaf.base}/etc/keycloak-hawtio.json
fuse.directAuthKeycloakConfig=${karaf.base}/etc/keycloak-direct-access.json
````

5) Copy keycloak-hawtio.json and keycloak-direct-access.json into fuse. File keycloak-hawtio.json is currently used for adapters 
on both client (keycloak.js) and server (JAAS Login module) side.

```shell
cp $BASE_DIR/hawtio/sample-keycloak-integration/keycloak-hawtio.json $FUSE_HOME/etc/
cp $BASE_DIR/hawtio/sample-keycloak-integration/keycloak-direct-access.json $FUSE_HOME/etc/
````
 
6) Run Fuse or Karaf. 

```shell
cd $FUSE_HOME/bin
./fuse
````

Replace with './karaf' if you are on plain Apache Karaf

7) If you are on fuse, you need to first uninstall old hawtio (This step is not needed on plain Apache karaf as it hasn't hawtio installed by default)
So in opened karaf terminal do this:

```shell
features:uninstall hawtio
features:uninstall hawtio-core
features:uninstall hawtio-karaf-terminal
features:uninstall hawtio-maven-indexer
features:removeurl mvn:io.hawt/hawtio-karaf/1.2-redhat-379/xml/features
````

8) Install newly build hawtio with keycloak integration

```shell
features:addurl mvn:io.hawt/hawtio-karaf/1.5-SNAPSHOT/xml/features
features:install hawtio
features:install hawtio-keycloak
````

9) Go to "http://localhost:8181" (or "http://localhost:8181/hawtio" on karaf) and login in keycloak as root or john to see hawtio admin console. If you login as mary, you should receive 'forbidden' error in hawtio

10) If you want to enable SSH login based on user credentials from keycloak, then you can also change these 2 properties (really needed just for SSH, not for hawtio) 
in file $FUSE_HOME/etc/org.apache.karaf.shell.cfg:

```shell
sshRealm=keycloak
sshRole=org.keycloak.adapters.jaas.RolePrincipal:admin
````

Now let's type this from your terminal:

```shell
ssh -p 8101 root@localhost
````

And login with password 'password' . Note that users john and mary don't have SSH access as they don't have 'admin' role. 

NOTE: For fuse I needed to explicitly disable public key authentication by:
```shell
ssh -o PubkeyAuthentication=no -p 8101 root@localhost
````

Hawtio integration on standalone Jetty or Tomcat
-----------------------------------------
It's working :) Instructions will be added later...
 
