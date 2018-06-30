Welcome to TightBlog! This project started off in May 2015 as a fork of the Apache Roller project, of which I contributed for about 2 1/2 years 
before deciding to go my own way due to differing ideas of what would make the best blogging product.  As of 30 June 2018, <a href="https://github.com/gmazza/tightblog/releases">Release 3.0.1</a> is available.

TightBlog strives to be the cleanest and simplest implementation of a Java based blog server, suitable either for direct use or
incorporation, as an Apache-licensed open source project, into larger projects.  Specifically, its goal is to satisfy all the needs of 80% of bloggers while
avoiding seldom-requested functionality that creates maintenance burdens, distracting one from core functionality and hence doing more harm than good.

This more realistic goal--along with adopting the Spring framework, REST, AngularJS and other code modernizations--has allowed TightBlog to slim down considerably from its parent, as can be seen in the following chart:

|Product|Released|Database Tables|Java Source Files|JSP Files|Lines Of Code|
|-----|-----|-----|-----|-----|-----|
|Apache Roller 5.1.2|1 Mar 2015|33|493|96|95.7K|
|TightBlog 1.0|17 July 2016|17|187|55|48.5K|
|TightBlog 2.0|4 June 2017|14|151|37|43.7K|
|TightBlog 2.0.4|12 Febuary 2018|13|146|37|42.9K|
|TightBlog 3.0|23 June 2018|12|119|37|35.8K|

(Lines of code--LOC--based on <a href="https://www.openhub.net/p/tightblog">OpenHub</a> stats.  Java source file count does not include unit test classes, however LOC do.)

In addition to the removal of older seldom used functionality many new helpful and modern features have been added:

* Bloggers may blog using <a href="http://commonmark.org/">CommonMark</a> in addition to standard HTML and Rich Text Editors.
* Blog entries have a "notes" field for the blogger to store anything helpful in maintaining the article.
* There is a new tag management screen allowing for renaming, merging, and deleting tags attached to blog entries, as well as adding a new tag to all articles already having a given tag.
* Both Category and Tag management tabs now list the number of blog entries associated with the category or tag, as well as publish dates of the first and last entries of each.
* A new "search.enabled" setting has been added to static configuration allowing for shutting off the Lucene indexer used for blog searching, useful in saving processing/space for when you're relying on third party indexing tools like Google Custom Search instead.
* Commenters who request "notify me" to receive emails of future comments for a particular blog entry now receive a link at the bottom of the email to shut off future notifications
* There is a new BLOGCREATOR global role separated from the earlier BLOGGER role.  While both roles allow a blogger full administration of his weblog (whether created by an admin for the blogger or the blogger himself) only users with the former role can create new weblogs.   
* Commenters who are logged-in bloggers now have their blogger ID stored with the comment, simplifying comment entry and allowing for different styling of comments (e.g., different background color for comments made by the blogger on his own blog).
* The blog template engine (used for customized themes) now uses modern Thymeleaf 3.0 instead of Apache Velocity.
* All emails sent are in HTML format and customizable by modifying the Thymeleaf templates in the webapp/thymeleaf/emails folder.
* Login Multifactor Authentication (MFA) with Google Authenticator support added (Admin setting provided to either require it for all bloggers--the default--or have it disabled).

To obtain the source code:
* 3.0.x branch (current): git clone git@github.com:gmazza/tightblog.git
* Release 2.0.x branch: https://github.com/gmazza/tightblog/tree/release2.0.4
* source for a specific release: https://github.com/gmazza/tightblog/releases

To build the application (app/target/tightblog.war) with Maven and Java 8:
  `mvn clean install` from the TightBlog root.

It's *very* quick and simple to try out TightBlog locally, to determine if this is a product you would like to blog with
before proceeding with an actual install.  After building the distribution via `mvn clean install`, navigate to the `app` folder and run `mvn jetty:run`,
and view http://localhost:8080/tightblog from a browser.  From there you can register an account, create a sample blog and some entries,
create and view comments, modify templates, etc., everything you can do with production TightBlog.  Each time it is run,
"mvn jetty:run" creates a new in-memory temporary database that exists until you Ctrl-Z out of the terminal window running this command.
(Note for simplicity the default does not demo Google Authenticator MFA, if desired modify the test/tightblog-jettyrun.properties as given in its comments to activate.)

For actual installations on Tomcat or other servlet container, please read the <a href="https://github.com/gmazza/tightblog/wiki">Install pages</a> on the TightBlog Wiki.
