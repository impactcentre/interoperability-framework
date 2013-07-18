/*
 *  Copyright 2011 The IMPACT Project Consortium.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package eu.impact_project.iif.tw.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import eu.impact_project.iif.tw.gen.GeneratorException;

/**
 * PropertyUtils
 * @author shsdev https://github.com/shsdev
 * @version 0.5
 */
public class PropertyUtil {

    private static Logger logger = Logger.getLogger(PropertyUtil.class.getName());
    private Properties properties;
    private HashMap<String, String> map;

    /**
     * Construct the property utils object from the properties file
     * @param propertiesFile a string path to a properties file
     * @throws GeneratorException
     */
    public PropertyUtil(String propertiesFile) throws GeneratorException {
        try {
            properties = new Properties();
            properties.load(new FileInputStream(propertiesFile));
            logger.debug("Property file \"" + propertiesFile + "\" loaded.");
        } catch (IOException ex) {
            throw new GeneratorException("Unable to load properties file!");
        }
    }

    /**
     * @return the properties key value pair map
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> getKeyValuePairs() {
        map = new HashMap<String, String>((Map) properties);
        return map;
    }

    /**
     * get a property value by key
     * @param key the property key
     * @return the property value
     */
    public String getProp(String key) {
        String val = properties.getProperty(key);
        return val;
    }
}
