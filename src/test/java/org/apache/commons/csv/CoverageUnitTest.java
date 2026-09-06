/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.csv;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Additional unit test suite to ensure comprehensive code coverage for JaCoCo and SonarCloud.
 */
public class CoverageUnitTest {

    @Test
    void testCSVFormatAndParserCoverage() throws IOException {
        final String csvData = "Name,Age,City\nAlice,30,New York\nBob,25,Los Angeles\n";
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Name", "Age", "City")
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();

        try (final CSVParser parser = CSVParser.parse(csvData, format)) {
            final List<CSVRecord> records = parser.getRecords();
            assertEquals(2, records.size());

            final CSVRecord record1 = records.get(0);
            assertEquals("Alice", record1.get("Name"));
            assertEquals("30", record1.get("Age"));
            assertEquals("New York", record1.get("City"));
            assertEquals(1, record1.getRecordNumber());
            assertTrue(record1.isMapped("Name"));
            assertTrue(record1.isSet("City"));
            assertEquals(3, record1.size());

            final CSVRecord record2 = records.get(1);
            assertEquals("Bob", record2.get("Name"));
            assertEquals(2, record2.getRecordNumber());
        }
    }

    @Test
    void testCSVPrinterCoverage() throws IOException {
        final StringWriter writer = new StringWriter();
        final CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Col1", "Col2")
                .setQuoteMode(QuoteMode.ALL)
                .build();

        try (final CSVPrinter printer = new CSVPrinter(writer, format)) {
            printer.printRecord("Value1", "Value2");
            printer.printRecord("Value3", "Value4");
            printer.flush();
        }

        final String result = writer.toString();
        assertNotNull(result);
        assertTrue(result.contains("Value1"));
        assertTrue(result.contains("Value2"));
    }

    @Test
    void testEnumsAndTokenCoverage() {
        assertNotNull(QuoteMode.valueOf("ALL"));
        assertNotNull(DuplicateHeaderMode.valueOf("ALLOW_ALL"));

        final Token token = new Token();
        token.reset();
        assertEquals(Token.Type.INVALID, token.type);
    }
}
