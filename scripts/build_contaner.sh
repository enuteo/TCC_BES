mvn clean install -U -DskipTests \
&& mvn spring-boot:build-image -Dspring-boot.build-image.imageName=localhost:5000/nome-da-imagem:tag -DskipTests \
&& docker push localhost:5000/nome-da-imagem:tag