Powernate HBM-2-PDM exporter
============================

Powernate - Automatic Hibernate Mapping to Sybase Powerdesigner Physical Data Model exporter.

Powernate is a useful tool for Sybase Powerdesigner users who are developing Hibernate projects.  

Create (or update) your Powerdesigner Physical Data Model (PDM) instantly from your existing Hibernate project with few clicks.

Powernate also features a full automatic generator of entity relationship diagrams, allowing the user to export the best visual arrangement of the model to Powerdesigner.

How it works
------------

See **simpledemoapp** for a demonstration.

1. Build (run `mvn package`) and add *powernate* JAR in your project dependencies;
2. Execute class **VisualPowernate**'s main method and follow instructions.


## New features in 1.0.1
- Now supporting Hibernate 4;
- Added an automatic classpath entity scanner;
- New in-GUI configurations.

## Tricks and Quirks
See files **powernate.properties**, **hibernate.cfg.xml** and others inside the Jar! It allows some fine-tuning customization for your projects.
