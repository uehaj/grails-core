package org.codehaus.groovy.grails.web.servlet.mvc

import org.springframework.mock.web.MockHttpServletRequest

class GrailsParameterMapTests extends GroovyTestCase {

    GrailsParameterMap theMap
    MockHttpServletRequest mockRequest = new MockHttpServletRequest()

    void testParseRequestBodyForPutRequest() {
        def request = new MockHttpServletRequest()
        request.content = 'foo=bar&one=two'.bytes
        request.method = 'PUT'
        request.contentType = "application/x-www-form-urlencoded"

        def params = new GrailsParameterMap(request)

        assert 'bar' == params.foo
        assert 'two' == params.one


        params = new GrailsParameterMap(request)
        assert params.foo == null // should be null, request can't be parsed twice

        request.content = 'foo='.bytes
        request.removeAttribute(GrailsParameterMap.REQUEST_BODY_PARSED)

        params = new GrailsParameterMap(request)

        assert '' == params.foo
    }

    void testPlusOperator() {
        mockRequest.addParameter("album", "Foxtrot")

        def originalMap = new GrailsParameterMap(mockRequest)

        def newMap = originalMap + [vocalist: 'Peter']
        assertTrue originalMap.containsKey('album')
        assertFalse originalMap.containsKey('vocalist')
        assertTrue newMap.containsKey('album')
        assertTrue newMap.containsKey('vocalist')
    }

    void testMultiDimensionParamsWithUnderscore() {
        mockRequest.addParameter("a.b.c", "on")
        mockRequest.addParameter("_a.b.c", "")
        theMap = new GrailsParameterMap(mockRequest)
        assert theMap['a.b.c'] == "on"
        assert theMap['_a.b.c'] == ""
        assert theMap['a'] instanceof Map
        assert theMap['a']['b'] instanceof Map
        assert theMap['a']['b']['c'] == "on"
        assert theMap['a']['_b.c'] == ""
        assert theMap['a']['b']['_c'] == ""
    }

    void testConversionHelperMethods() {
        def map = new GrailsParameterMap(mockRequest)

        map.zero = "0"
        map.one = "1"
        map.bad = "foo"
        map.decimals = "1.4"
        map.bool = "true"
        map.aList = [1,2]
        map.array = ["one", "two" ] as String[]
        map.longNumber = 1234567890
        map.z = 'z'

        assertEquals(["1"], map.list("one"))
        assertEquals([1,2], map.list("aList"))
        assertEquals(["one","two"], map.list("array"))
        assertEquals([], map.list("nonexistant"))

        assertEquals 1, map.byte('one')
        assertEquals(-46, map.byte('longNumber')) // overflows
        assertNull map.byte("test")
        assertNull map.byte("bad")
        assertNull map.byte("nonexistant")
        assertEquals 0, map.byte('zero')
        assertEquals 1, map.byte('one', 42 as Byte)
        assertEquals 0, map.byte('zero', 42 as Byte)
        assertEquals 42, map.byte('bad', 42 as Byte)
        assertEquals 42, map.byte('nonexistent', 42 as Byte)
        assertEquals 1, map.byte('one', 42)
        assertEquals 0, map.byte('zero', 42)
        assertEquals 42, map.byte('bad', 42)
        assertEquals 42, map.byte('nonexistent', 42)

        assertEquals '1', map.char('one')
        assertNull map.char('longNumber')
        assertNull map.char("test")
        assertNull map.char("bad")
        assertNull map.char("nonexistant")
        assertEquals '0', map.char('zero')
        assertEquals '1', map.char('one', 'A' as Character)
        assertEquals '0', map.char('zero', 'A' as Character)
        assertEquals 'A', map.char('bad', 'A' as Character)
        assertEquals 'A', map.char('nonexistent', 'A' as Character)
        assertEquals '1', map.char('one', (char)'A')
        assertEquals '0', map.char('zero', (char)'A')
        assertEquals 'A', map.char('bad', (char)'A')
        assertEquals 'A', map.char('nonexistent', (char)'A')
        assertEquals 'z', map.char('z')
        assertEquals 'z', map.char('z', (char)'A')
        assertEquals 'z', map.char('z', 'A' as Character)

        assertEquals 1, map.int('one')
        assertNull map.int("test")
        assertNull map.int("bad")
        assertNull map.int("nonexistant")
        assertEquals 0, map.int('zero')
        assertEquals 1, map.int('one', 42)
        assertEquals 0, map.int('zero', 42)
        assertEquals 42, map.int('bad', 42)
        assertEquals 42, map.int('nonexistent', 42)

        assertEquals 1L, map.long('one')
        assertNull map.long("test")
        assertNull map.long("bad")
        assertNull map.long("nonexistant")
        assertEquals 0L, map.long('zero')
        assertEquals 1L, map.long('one', 42L)
        assertEquals 0L, map.long('zero', 42L)
        assertEquals 42L, map.long('bad', 42L)
        assertEquals 42L, map.long('nonexistent', 42L)

        assertEquals 1, map.short('one')
        assertNull map.short("test")
        assertNull map.short("bad")
        assertNull map.short("nonexistant")
        assertEquals 0, map.short('zero')
        assertEquals 1, map.short('one', 42 as Short)
        assertEquals 0, map.short('zero', 42 as Short)
        assertEquals 42, map.short('bad', 42 as Short)
        assertEquals 42, map.short('nonexistent', 42 as Short)
        assertEquals 1, map.short('one', 42)
        assertEquals 0, map.short('zero', 42)
        assertEquals 42, map.short('bad', 42)
        assertEquals 42, map.short('nonexistent', 42)

        assertEquals 1.0, map.double('one')
        assertEquals 1.4, map.double('decimals')
        assertNull map.double("bad")
        assertNull map.double("nonexistant")
        assertEquals 0.0, map.double('zero')
        assertEquals 1.0, map.double('one', 42.0)
        assertEquals 0.0, map.double('zero', 42.0)
        assertEquals 42.0, map.double('bad', 42.0)
        assertEquals 42.0, map.double('nonexistent', 42.0)

        assertEquals 1.0, map.float('one')
        assertEquals 1.399999976158142, map.float('decimals')
        assertNull map.float("bad")
        assertNull map.float("nonexistant")
        assertEquals 0.0f, map.float('zero')
        assertEquals 1.0f, map.float('one', 42.0f)
        assertEquals 0.0f, map.float('zero', 42.0f)
        assertEquals 42.0f, map.float('bad', 42.0f)
        assertEquals 42.0f, map.float('nonexistent', 42.0f)

        assertEquals false, map.boolean('one')
        assertEquals true, map.boolean('nonexistent', Boolean.TRUE)
        assertEquals false, map.boolean('nonexistent', Boolean.FALSE)
        assertEquals true, map.boolean('bool')
        assertNull map.boolean("nonexistant")
    }

    void testAutoEvaluateBlankDates() {
        mockRequest.addParameter("foo", "date.struct")
        mockRequest.addParameter("foo_year", "")
        mockRequest.addParameter("foo_month", "")


        theMap = new GrailsParameterMap(mockRequest)
        assert theMap['foo'] == null : "should be null"
    }

    void testAutoEvaluateDates() {
        mockRequest.addParameter("foo", "date.struct")
        mockRequest.addParameter("foo_year", "2007")
        mockRequest.addParameter("foo_month", "07")

        theMap = new GrailsParameterMap(mockRequest)

        assert theMap['foo'] instanceof Date : "Should have returned a date but was a ${theMap['foo']}!"
        def cal = new GregorianCalendar()
        cal.setTime(theMap['foo'])

        assert 2007 == cal.get(Calendar.YEAR) : "Year should be 2007"
    }

    void testIterateOverMapContainingDate() {
        mockRequest.addParameter("stuff", "07")
        mockRequest.addParameter("foo", "date.struct")
        mockRequest.addParameter("foo_year", "2007")
        mockRequest.addParameter("foo_month", "07")
        mockRequest.addParameter("bar", "07")

        theMap = new GrailsParameterMap(mockRequest)

        def params = new GrailsParameterMap(mockRequest)
        for (Object o : theMap.keySet()) {
            String name = (String) o
            params.put(name, theMap.get(name))
        }
    }

    void testMultiDimensionParams() {
        mockRequest.addParameter("a.b.c", "cValue")
        mockRequest.addParameter("a.b", "bValue")
        mockRequest.addParameter("a.bc", "bcValue")
        mockRequest.addParameter("a.b.d", "dValue")
        mockRequest.addParameter("a.e.f", "fValue")
        mockRequest.addParameter("a.e.g", "gValue")
        theMap = new GrailsParameterMap(mockRequest)
        assert theMap['a'] instanceof Map
        assert theMap.a.b == "bValue"
        assert theMap.a.'b.c' == "cValue"
        assert theMap.a.'bc' == "bcValue"
        assert theMap.a.'b.d' == "dValue"

        assert theMap.a['e'] instanceof Map
        assert theMap.a.e.f == "fValue"
        assert theMap.a.e.g == "gValue"
    }

    void testToQueryString() {
        mockRequest.addParameter("name", "Dierk Koenig")
        mockRequest.addParameter("dob", "01/01/1970")
        theMap = new GrailsParameterMap(mockRequest)

        def queryString = theMap.toQueryString()

        assertTrue queryString.startsWith('?')
        queryString = queryString[1..-1].split('&')

        assert queryString.find { it == 'name=Dierk+Koenig' }
        assert queryString.find { it == 'dob=01%2F01%2F1970' }
    }

    void testSimpleMappings() {
        mockRequest.addParameter("test", "1")
        theMap = new GrailsParameterMap(mockRequest)

        assertEquals "1", theMap['test']
    }

    void testToQueryStringWithMultiD() {
        mockRequest.addParameter("name", "Dierk Koenig")
        mockRequest.addParameter("dob", "01/01/1970")
        mockRequest.addParameter("address.postCode", "345435")
        theMap = new GrailsParameterMap(mockRequest)

        def queryString = theMap.toQueryString()

        assertTrue queryString.startsWith('?')
        queryString = queryString[1..-1].split('&')

        assert queryString.find { it == 'name=Dierk+Koenig' }
        assert queryString.find { it == 'dob=01%2F01%2F1970' }
        assert queryString.find { it == 'address.postCode=345435' }
    }

    void testCloning() {
        mockRequest.addParameter("name", "Dierk Koenig")
        mockRequest.addParameter("dob", "01/01/1970")
        mockRequest.addParameter("address.postCode", "345435")
        theMap = new GrailsParameterMap(mockRequest)

        def theClone = theMap.clone()

        assertEquals("clone size should be the same as original", theMap.size(), theClone.size())

        theMap.each { k, v ->
            assertEquals("the clone should have the same value for $k as the original", theMap[k], theClone[k])
        }
    }
}