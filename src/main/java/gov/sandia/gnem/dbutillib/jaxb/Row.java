//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.0-b26-ea3 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2006.01.11 at 02:34:16 PM MST 
//


package gov.sandia.gnem.dbutillib.jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Row
 *
 * <p>Java class for Row element declaration.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;element name="Row">
 *   &lt;complexType>
 *     &lt;complexContent>
 *       &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *         &lt;sequence>
 *           &lt;element name="rowId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *           &lt;element name="field" maxOccurs="unbounded">
 *             &lt;complexType>
 *               &lt;complexContent>
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                   &lt;attribute name="col" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                   &lt;attribute name="val" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;/restriction>
 *               &lt;/complexContent>
 *             &lt;/complexType>
 *           &lt;/element>
 *           &lt;element name="children" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="parents" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *           &lt;element name="dataSourceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/sequence>
 *       &lt;/restriction>
 *     &lt;/complexContent>
 *   &lt;/complexType>
 * &lt;/element>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "rowId",
        "field",
        "children",
        "parents",
        "dataSourceName"
})
@XmlRootElement(name = "Row")
public class Row {

    protected String rowId;
    protected List<Field> field;
    protected String children;
    protected String parents;
    protected String dataSourceName;

    /**
     * Gets the value of the rowId property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Sets the value of the rowId property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRowId(String value) {
        this.rowId = value;
    }

    /**
     * Gets the value of the field property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<Field>();
        }
        return this.field;
    }

    /**
     * Gets the value of the children property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getChildren() {
        return children;
    }

    /**
     * Sets the value of the children property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setChildren(String value) {
        this.children = value;
    }

    /**
     * Gets the value of the parents property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getParents() {
        return parents;
    }

    /**
     * Sets the value of the parents property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setParents(String value) {
        this.parents = value;
    }

    /**
     * Gets the value of the dataSourceName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getDataSourceName() {
        return dataSourceName;
    }

    /**
     * Sets the value of the dataSourceName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setDataSourceName(String value) {
        this.dataSourceName = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="col" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="val" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Field {

        @XmlAttribute(required = true)
        protected String col;
        @XmlAttribute(required = true)
        protected String val;

        /**
         * Gets the value of the col property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getCol() {
            return col;
        }

        /**
         * Sets the value of the col property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setCol(String value) {
            this.col = value;
        }

        /**
         * Gets the value of the val property.
         *
         * @return possible object is
         * {@link String }
         */
        public String getVal() {
            return val;
        }

        /**
         * Sets the value of the val property.
         *
         * @param value allowed object is
         *              {@link String }
         */
        public void setVal(String value) {
            this.val = value;
        }

    }

}