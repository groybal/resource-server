/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.09.24 at 12:43:26 PM CDT 
//


package org.jasig.resource.aggr.om;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for included.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="included">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="both"/>
 *     &lt;enumeration value="aggregated"/>
 *     &lt;enumeration value="plain"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "included")
@XmlEnum
public enum Included {


    /**
     * Include the resource when using both aggregated and plain resources
     * 
     */
    @XmlEnumValue("both")
    BOTH("both"),

    /**
     * Only include the resource when using aggregated resources
     * 
     */
    @XmlEnumValue("aggregated")
    AGGREGATED("aggregated"),

    /**
     * Only include the resource when using plain resources
     * 
     */
    @XmlEnumValue("plain")
    PLAIN("plain");
    private final String value;

    Included(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Included fromValue(String v) {
        for (Included c: Included.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
