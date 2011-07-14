REVOKE ALL PRIVILEGES ON * . * FROM 'storm'@'localhost';

REVOKE ALL PRIVILEGES ON `realm` . * FROM 'storm'@'localhost';

REVOKE GRANT OPTION ON `realm` . * FROM 'storm'@'localhost';

DELETE FROM `user` WHERE CONVERT( User USING utf8 ) = CONVERT( 'storm' USING utf8 ) AND CONVERT( Host USING utf8 ) = CONVERT( 'localhost' USING utf8 ) ;

DROP DATABASE IF EXISTS `realm` ;
