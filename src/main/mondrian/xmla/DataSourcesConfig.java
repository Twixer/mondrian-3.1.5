/*
// This java file was automatically generated
// from XOM model 'datasourcesconfig'
// on Thu Jun 13 23:42:34 CEST 2013
// Do not edit this file by hand.
*/

package mondrian.xmla;
/**
 * This is the XML model for XMLA DataSources Configuration.
 * Revision is $Id: //open/mondrian-release/3.1/src/main/mondrian/xmla/DataSourcesConfig.xml#2 $
 * <p>This class was generated from XOM model 'datasourcesconfig' on Thu Jun 13 23:42:34 CEST 2013
 */
public class DataSourcesConfig {

	public static java.lang.Class getXMLDefClass()
	{
		return DataSourcesConfig.class;
	}

	public static String[] _elements = {
		"DataSources",
		"DataSource",
		"DataSourceName",
		"DataSourceDescription",
		"URL",
		"DataSourceInfo",
		"ProviderName",
		"ProviderType",
		"AuthenticationMode",
		"Definition",
		"Catalogs",
		"Catalog"
	};

	/**
	 * The list of data sources supported by XMLA service.
	 */
	public static class DataSources extends org.eigenbase.xom.ElementDef
	{
		public DataSources()
		{
		}

		public DataSources(org.eigenbase.xom.DOMWrapper _def)
			throws org.eigenbase.xom.XOMException
		{
			try {
				org.eigenbase.xom.DOMElementParser _parser = new org.eigenbase.xom.DOMElementParser(_def, "", DataSourcesConfig.class);
				org.eigenbase.xom.NodeDef[] _tempArray;
				_tempArray = _parser.getArray(DataSource.class, 0, 0);
				dataSources = new DataSource[_tempArray.length];
				for (int _i = 0; _i < dataSources.length; _i++)
					dataSources[_i] = (DataSource)_tempArray[_i];
			} catch(org.eigenbase.xom.XOMException _ex) {
				throw new org.eigenbase.xom.XOMException("In " + getName() + ": " + _ex.getMessage());
			}
		}


		/**
		 * The list of data sources supported by XMLA service.
		 */
		public DataSource[] dataSources;  //optional array

		public String getName()
		{
			return "DataSources";
		}

		public void display(java.io.PrintWriter _out, int _indent)
		{
			_out.println(getName());
			displayElementArray(_out, "dataSources", dataSources, _indent+1);
		}
		public void displayXML(org.eigenbase.xom.XMLOutput _out, int _indent)
		{
			_out.beginTag("DataSources", new org.eigenbase.xom.XMLAttrVector()
				);
			displayXMLElementArray(_out, dataSources);
			_out.endTag("DataSources");
		}
		public boolean displayDiff(org.eigenbase.xom.ElementDef _other, java.io.PrintWriter _out, int _indent)
		{
			DataSources _cother = (DataSources)_other;
			boolean _diff = displayElementArrayDiff("dataSources", dataSources, _cother.dataSources, _out, _indent+1);
			return _diff;
		}
	}

	/**
	 * Definition of a data source.
	 */
	public static class DataSource extends org.eigenbase.xom.ElementDef
	{
		public DataSource()
		{
		}

		public DataSource(org.eigenbase.xom.DOMWrapper _def)
			throws org.eigenbase.xom.XOMException
		{
			try {
				org.eigenbase.xom.DOMElementParser _parser = new org.eigenbase.xom.DOMElementParser(_def, "", DataSourcesConfig.class);
				name = _parser.getString(DataSourceName, true);
				description = _parser.getString(DataSourceDescription, true);
				url = _parser.getString(URL, true);
				dataSourceInfo = _parser.getString(DataSourceInfo, true);
				providerName = _parser.getString(ProviderName, true);
				providerType = _parser.getString(ProviderType, true);
				authenticationMode = _parser.getString(AuthenticationMode, true);
				catalogs = (Catalogs)_parser.getElement(Catalogs.class, true);
			} catch(org.eigenbase.xom.XOMException _ex) {
				throw new org.eigenbase.xom.XOMException("In " + getName() + ": " + _ex.getMessage());
			}
		}


		/**
		 * Name.
		 */
		public String name;  //required element
		/**
		 * Description.
		 */
		public String description;  //required element
		/**
		 * URL of Web Services invocation.
		 */
		public String url;  //required element
		/**
		 * ConnectString of Mondrian (minus the catalog entry).
		 * Required, but catalog may override.
		 */
		public String dataSourceInfo;  //required element
		/**
		 * Customized Service Provider Name.
		 */
		public String providerName;  //required element
		/**
		 * Ignored. Only return "MDP" for DISCOVER_DATASOURCES.
		 */
		public String providerType;  //required element
		/**
		 * Ignored. Only return "Unauthenticated" for DISCOVER_DATASOURCES.
		 */
		public String authenticationMode;  //required element
		/**
		 * One or more Catalogs.
		 */
		public Catalogs catalogs;  //required element

		public String getName()
		{
			return "DataSource";
		}

		public void display(java.io.PrintWriter _out, int _indent)
		{
			_out.println(getName());
			displayString(_out, "name", name, _indent+1);
			displayString(_out, "description", description, _indent+1);
			displayString(_out, "url", url, _indent+1);
			displayString(_out, "dataSourceInfo", dataSourceInfo, _indent+1);
			displayString(_out, "providerName", providerName, _indent+1);
			displayString(_out, "providerType", providerType, _indent+1);
			displayString(_out, "authenticationMode", authenticationMode, _indent+1);
			displayElement(_out, "catalogs", catalogs, _indent+1);
		}
		public void displayXML(org.eigenbase.xom.XMLOutput _out, int _indent)
		{
			_out.beginTag("DataSource", new org.eigenbase.xom.XMLAttrVector()
				);
			displayXMLString(_out, "DataSourceName", name);
			displayXMLString(_out, "DataSourceDescription", description);
			displayXMLString(_out, "URL", url);
			displayXMLString(_out, "DataSourceInfo", dataSourceInfo);
			displayXMLString(_out, "ProviderName", providerName);
			displayXMLString(_out, "ProviderType", providerType);
			displayXMLString(_out, "AuthenticationMode", authenticationMode);
			displayXMLElement(_out, catalogs);
			_out.endTag("DataSource");
		}
		public boolean displayDiff(org.eigenbase.xom.ElementDef _other, java.io.PrintWriter _out, int _indent)
		{
			DataSource _cother = (DataSource)_other;
			boolean _diff = displayStringDiff("name", name, _cother.name, _out, _indent+1);
			_diff = _diff && displayStringDiff("description", description, _cother.description, _out, _indent+1);
			_diff = _diff && displayStringDiff("url", url, _cother.url, _out, _indent+1);
			_diff = _diff && displayStringDiff("dataSourceInfo", dataSourceInfo, _cother.dataSourceInfo, _out, _indent+1);
			_diff = _diff && displayStringDiff("providerName", providerName, _cother.providerName, _out, _indent+1);
			_diff = _diff && displayStringDiff("providerType", providerType, _cother.providerType, _out, _indent+1);
			_diff = _diff && displayStringDiff("authenticationMode", authenticationMode, _cother.authenticationMode, _out, _indent+1);
			_diff = _diff && displayElementDiff("catalogs", catalogs, _cother.catalogs, _out, _indent+1);
			return _diff;
		}
		// BEGIN pass-through code block ---
public static final String PROVIDER_TYPE_TDP = "TDP";
            public static final String PROVIDER_TYPE_MDP = "MDP";
            public static final String PROVIDER_TYPE_DMP = "DMP";
            public static final String AUTH_MODE_UNAUTHENTICATED = "Unauthenticated";
            public static final String AUTH_MODE_AUTHENTICATED = "Authenticated";
            public static final String AUTH_MODE_INTEGRATED = "Integrated";

            public String getDataSourceName() {
                return name;
            }

            public String getDataSourceDescription() {
                return description;
            }

            public String getURL() {
                return url;
            }

            public String getDataSourceInfo() {
                return dataSourceInfo;
            }

            public String getProviderName() {
                return providerName;
            }

            public String[] getProviderType() {
                return new String[] {PROVIDER_TYPE_MDP};
            }

            public String getAuthenticationMode() {
                return authenticationMode;
            }
		// END pass-through code block ---
	}

	public static final String DataSourceName = "DataSourceName";

	public static final String DataSourceDescription = "DataSourceDescription";

	public static final String URL = "URL";

	public static final String DataSourceInfo = "DataSourceInfo";

	public static final String ProviderName = "ProviderName";

	public static final String ProviderType = "ProviderType";

	public static final String AuthenticationMode = "AuthenticationMode";

	public static final String Definition = "Definition";

	/**
	 * The list of catalogs associated with a data source.
	 */
	public static class Catalogs extends org.eigenbase.xom.ElementDef
	{
		public Catalogs()
		{
		}

		public Catalogs(org.eigenbase.xom.DOMWrapper _def)
			throws org.eigenbase.xom.XOMException
		{
			try {
				org.eigenbase.xom.DOMElementParser _parser = new org.eigenbase.xom.DOMElementParser(_def, "", DataSourcesConfig.class);
				org.eigenbase.xom.NodeDef[] _tempArray;
				_tempArray = _parser.getArray(Catalog.class, 0, 0);
				catalogs = new Catalog[_tempArray.length];
				for (int _i = 0; _i < catalogs.length; _i++)
					catalogs[_i] = (Catalog)_tempArray[_i];
			} catch(org.eigenbase.xom.XOMException _ex) {
				throw new org.eigenbase.xom.XOMException("In " + getName() + ": " + _ex.getMessage());
			}
		}


		/**
		 * The list of catalogs.
		 */
		public Catalog[] catalogs;  //optional array

		public String getName()
		{
			return "Catalogs";
		}

		public void display(java.io.PrintWriter _out, int _indent)
		{
			_out.println(getName());
			displayElementArray(_out, "catalogs", catalogs, _indent+1);
		}
		public void displayXML(org.eigenbase.xom.XMLOutput _out, int _indent)
		{
			_out.beginTag("Catalogs", new org.eigenbase.xom.XMLAttrVector()
				);
			displayXMLElementArray(_out, catalogs);
			_out.endTag("Catalogs");
		}
		public boolean displayDiff(org.eigenbase.xom.ElementDef _other, java.io.PrintWriter _out, int _indent)
		{
			Catalogs _cother = (Catalogs)_other;
			boolean _diff = displayElementArrayDiff("catalogs", catalogs, _cother.catalogs, _out, _indent+1);
			return _diff;
		}
	}

	public static class Catalog extends org.eigenbase.xom.ElementDef
	{
		public Catalog()
		{
		}

		public Catalog(org.eigenbase.xom.DOMWrapper _def)
			throws org.eigenbase.xom.XOMException
		{
			try {
				org.eigenbase.xom.DOMElementParser _parser = new org.eigenbase.xom.DOMElementParser(_def, "", DataSourcesConfig.class);
				name = (String)_parser.getAttribute("name", "String", null, null, true);
				dataSourceInfo = _parser.getString(DataSourceInfo, false);
				definition = _parser.getString(Definition, true);
			} catch(org.eigenbase.xom.XOMException _ex) {
				throw new org.eigenbase.xom.XOMException("In " + getName() + ": " + _ex.getMessage());
			}
		}

		public String name;  // required attribute

		/**
		 * ConnectString of Mondrian (minus the catalog entry).
		 * This entry is optional; if present, it overrides the
		 * DataSourceInfo within the DataSource.
		 */
		public String dataSourceInfo;  //optional element
		/**
		 * URI of the schema definition file, for example
		 * "/WEB-INF/schema/Marketing.xml".
		 */
		public String definition;  //required element

		public String getName()
		{
			return "Catalog";
		}

		public void display(java.io.PrintWriter _out, int _indent)
		{
			_out.println(getName());
			displayAttribute(_out, "name", name, _indent+1);
			displayString(_out, "dataSourceInfo", dataSourceInfo, _indent+1);
			displayString(_out, "definition", definition, _indent+1);
		}
		public void displayXML(org.eigenbase.xom.XMLOutput _out, int _indent)
		{
			_out.beginTag("Catalog", new org.eigenbase.xom.XMLAttrVector()
				.add("name", name)
				);
			displayXMLString(_out, "DataSourceInfo", dataSourceInfo);
			displayXMLString(_out, "Definition", definition);
			_out.endTag("Catalog");
		}
		public boolean displayDiff(org.eigenbase.xom.ElementDef _other, java.io.PrintWriter _out, int _indent)
		{
			Catalog _cother = (Catalog)_other;
			boolean _diff = displayAttributeDiff("name", name, _cother.name, _out, _indent+1);
			_diff = _diff && displayStringDiff("dataSourceInfo", dataSourceInfo, _cother.dataSourceInfo, _out, _indent+1);
			_diff = _diff && displayStringDiff("definition", definition, _cother.definition, _out, _indent+1);
			return _diff;
		}
		// BEGIN pass-through code block ---
private DataSource dataSource;

            /**
              * Sets this catalog's parent data source, so that the data
              * source's definition can be inherited.
              */
            public void setDataSource(DataSource dataSource) {
                this.dataSource = dataSource;
            }

            /**
              * Returns this catalog's parent data source.
              */
            public DataSource getDataSource() {
                return dataSource;
            }

            /**
              * Returns this catalog's connect string, or its parent's
              * connect string if not set.
              */
            public String getDataSourceInfo() {
                if (dataSourceInfo != null) {
                    return dataSourceInfo;
                } else if (dataSource != null) {
                    return dataSource.dataSourceInfo;
                } else {
                    return null;
                }
            }
		// END pass-through code block ---
	}


}
