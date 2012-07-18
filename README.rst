nfms-portal
===========

The public portal for `nfms4redd <http://nfms4redd.org/>`_ infrastructure.

This is a customizable web-based map viewer that will let users access:

* Base data layers (such as Blue Marble or Landsat).
* Custom overlays (such as countrie's administrative boundaries).
* Time-dependant layers (automatically updated by the nfms4redd system).
* Pre-calculated charts based on layer time changes.


building nfms-portal
--------------------

Portal can be customized and localized for each implementing country.

Customization files are kept separate in ``webResources`` directory. When building a new customization, start from the ``sample_country`` profile under ``webResources``.

Maven is used to build the project and to merge the customization profile into the resulting .war file.

To change the customization profile, edit the ``<country>`` propery in ``pom.xml``, and run::

  mvn install

This will compile the customized portal, run test, and generate the deployable ``war`` file under ``target`` directory.


Customizing nfms-portal
-----------------------

To learn about portal customization, please follow the project's `technical documentation <http://nfms4redd.org/doc/html/portal/index.html>`_ (documentation sources also on `github <https://github.com/nfms4redd/nfms-documentation>`_).

