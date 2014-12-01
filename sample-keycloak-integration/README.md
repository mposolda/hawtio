Hawtio integration with Keycloak
================================

Enable integration on Karaf or JBoss Fuse
-----------------------------------------
This was tested with Apache Karaf 2.3.9 and JBoss Fuse 6.1.0-redhat379 . 

1) First checkout latest keycloak and build from sources. It's also needed to build distribution where are osgi bundles
        
```shell
git clone https://github.com/keycloak/keycloak.git
cd $BASE_DIR/keycloak
mvn clean install
cd distribution
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
````

5) Copy keycloak-hawtio.json and keycloak-direct-access.json into fuse. File keycloak-hawtio.json is currently used for adapters 
on both client (keycloak.js) and server (JAAS Login module) side. The file keycloak-direct-access.json is used for authentication of SSH or JMX RMI access with keycloak (actually not needed for hawtio).

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
````

9) Install keycloak into karaf/fuse

```shell
features:addurl mvn:org.keycloak/keycloak-osgi-features/1.1.0.Beta2-SNAPSHOT/xml/features
features:install keycloak-jaas
````

9) Go to "http://localhost:8181" (or "http://localhost:8181/hawtio" on karaf) and login in keycloak as root or john to see hawtio admin console. If you login as mary, you should receive 'forbidden' error in hawtio

SSH authentication with keycloak credentials
--------------------------------------------

1) If you want to enable SSH login based on user credentials from keycloak, then you can also change these 2 properties (really needed just for SSH, not for hawtio) 
in file $FUSE_HOME/etc/org.apache.karaf.shell.cfg:

```shell
sshRealm=keycloak
sshRole=org.keycloak.adapters.jaas.RolePrincipal:admin
````

2) Now let's type this from your terminal:

```shell
ssh -o PubkeyAuthentication=no -p 8101 root@localhost
````

And login with password 'password' . Note that users john and mary don't have SSH access as they don't have 'admin' role. 


JMX authentication with keycloak credentials
--------------------------------------------
This may be needed just in case if you really want to use jconsole or other external tool to perform remote connection to JMX through RMI. Otherwise it may 
be better to use just hawtio/jolokia as jolokia agent is installed in hawtio by default.
 
1) In file $FUSE_HOME/etc/org.apache.karaf.management.cfg you can change these 2 properties:

```shell
jmxRealm=keycloak
jmxRole=org.keycloak.adapters.jaas.RolePrincipal:admin
````

2) In jconsole you can fill URL like:

```shell
service:jmx:rmi://localhost:44444/jndi/rmi://localhost:1099/karaf-root
````

and credentials: root/password

Note again that john and mary are not able to login as they don't have admin role. Note that 'john' is still able to access MBeans remotely via HTTP (Hawtio), so MBeans 
are defacto not protected for him to see them.

Hawtio integration on standalone Jetty or Tomcat
-----------------------------------------
It's working :) Instructions will be added later...
 
