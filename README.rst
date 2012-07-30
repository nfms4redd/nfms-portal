nfms-portal
===========

The public portal for `nfms4redd <http://nfms4redd.org/>`_ infrastructure.

This is a customizable web-based map viewer that will let users access:

* Base data layers (such as Blue Marble or Landsat).
* Custom overlays (such as countrie's administrative boundaries).
* Time-dependant layers (automatically updated by the nfms4redd system).
* Pre-calculated charts based on layer time changes.


Customizing nfms-portal
-----------------------

Portal can be customized and localized for each implementing country. Customization
files are kept in a separate directory. Use the JVM property ``PORTAL_CONFIG_DIR``
to point to your custom files.

To create a new customization, you can start from the example ``default_config``
profile located under ``src/main/java/webapp/WEB-INF`` to your own directory.

Details on how to customize the portal can be found in the project's
`technical documentation <http://nfms4redd.org/doc/html/portal/index.html>`_.


Running, building and deploying nfms-portal
-------------------------------------------

To run portal using maven, set the ``<portal_config_dir>`` property in ``pom.xml``,
and run::

  mvn jetty:run

To deploy in a tomcat server, run::

  mvn install
  
And copy the war file generated under ``target`` into tomcat ``webapps``.

You will need to indicate where the ``PORTAL_CONFIG_DIR`` is located, using the ``-D``
option. For example, in `setenv`, add::

  -DPORTAL_CONFIG_DIR="/var/portal/drc"

  
Software license
----------------

nfms4redd Portal Interface - http://nfms4redd.org/

(C) 2012, FAO Forestry Department (http://www.fao.org/forestry/)

This application is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public
License as published by the Free Software Foundation;
version 3.0 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.
