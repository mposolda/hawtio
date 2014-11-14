Hawtio integration with Keycloak
================================

Enable integration on Karaf or JBoss Fuse
-----------------------------------------
This was tested with Apache Karaf 3.0.1 and JBoss Fuse 6.1.0-redhat379 . 

1) Checkout latest hawtio from 'mposolda' where is Keycloak integration enabled. Use branch 'hawtio-keycloak' :

```shell
cd $BASE_DIR
git clone https://github.com/mposolda/hawtio.git
cd hawtio
git checkout origin/hawtio-keycloak
mvn clean install 
````

2) Download keycloak appliance with wildfly from http://sourceforge.net/projects/keycloak/files/1.1.0.Final/keycloak-appliance-dist-all-1.1.0.Final.zip/download . 
Then unpack and run keycloak server on localhost:8081 and import hawtio-demo realm into it.

```shell
cd $BASE_DIR
unzip -q /downloads/keycloak-appliance-dist-all-1.1.0.Final.zip
cd keycloak-appliance-dist-all-1.1.0.Final/keycloak/bin/
./standalone.sh -Djboss.http.port=8081 -Dkeycloak.import=$BASE_DIR/hawtio/sample-keycloak-integration/demorealm.json
````

Realm has 'hawtio-client' application installed as public client. There are 2 realm roles 'admin' and 'viewer' . Names of these roles are same like 
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
hawtio.keycloakClientConfig=${karaf.base}/etc/keycloak-hawtio-client.json
````

5) Copy keycloak-hawtio.json and keycloak-hawtio-client.json into fuse. File keycloak-hawtio.json is currently used for adapters 
on server (JAAS Login module) side. File keycloak-hawtio-client.json is used on client (keycloak.js) side.

```shell
cp $BASE_DIR/hawtio/sample-keycloak-integration/keycloak-hawtio.json $FUSE_HOME/etc/
cp $BASE_DIR/hawtio/sample-keycloak-integration/keycloak-hawtio-client.json $FUSE_HOME/etc/
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
features:chooseurl hawtio 1.5-SNAPSHOT
features:install hawtio
````

9) Install keycloak into karaf/fuse

```shell
features:addurl mvn:org.keycloak/keycloak-osgi-features/1.2.0.Beta1-SNAPSHOT/xml/features
features:install keycloak-jaas
````

9) Go to "http://localhost:8181" (or "http://localhost:8181/hawtio" on karaf) and login in keycloak as root or john to see hawtio admin console. If you login as mary, you should receive 'forbidden' error in hawtio

Hawtio integration on standalone Jetty or Tomcat
-----------------------------------------
It's working :) Instructions will be added later...
 
