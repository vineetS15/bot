FROM tomcat
MAINTAINER vinever@gmail.com

ADD SkypeBot.war /usr/local/tomcat/webapps/

CMD ["catalina.sh", "run"]