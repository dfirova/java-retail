/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package product;

import static com.google.common.truth.Truth.assertThat;
import static product.ImportProductsGcs.importProductsFromGcs;
import static product.setup.ProductsCreateGcsBucket.createGcsBucketAndUploadData;

import com.google.cloud.ServiceOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.ExecutionException;

import com.google.cloud.Timestamp;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ImportProductsGcsTest {

  private ByteArrayOutputStream bout;
  private PrintStream originalPrintStream;

  @Before
  public void setUp() throws IOException, InterruptedException, ExecutionException {
    bout = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @Test
  public void testValidImportProductsGcs() throws IOException, InterruptedException {
    String projectId = ServiceOptions.getDefaultProjectId();
    String branchName =
        String.format(
            "projects/%s/locations/global/catalogs/default_catalog/branches/0", projectId);
    String bucketName = String.format("%s_products_tests_bucket_%s", projectId, Timestamp.now());
    String gcsBucket = String.format("gs://%s", bucketName);
    String gscProductsObject = "products.json";

    createGcsBucketAndUploadData(bucketName);

    importProductsFromGcs(branchName, bucketName, gcsBucket, gscProductsObject);

    String outputResult = bout.toString();

    assertThat(outputResult).contains("Import products from google cloud source request");
    assertThat(outputResult).contains("Number of successfully imported products:");
    assertThat(outputResult).contains("Number of failures during the importing: 0");
  }

  @Test
  public void testInvalidImportProductsGcs() throws IOException, InterruptedException {
    String projectId = ServiceOptions.getDefaultProjectId();
    String branchName =
        String.format(
            "projects/%s/locations/global/catalogs/default_catalog/branches/0", projectId);
    String bucketName = String.format("%s_products_tests_invalid_bucket_%s", projectId, Timestamp.now());
    String gcsBucket = String.format("gs://%s", bucketName);
    String gscProductsObject = "products_some_invalid.json";

    createGcsBucketAndUploadData(bucketName);
    importProductsFromGcs(branchName, bucketName, gcsBucket, gscProductsObject);

    String outputResult = bout.toString();

    assertThat(outputResult).contains("Import products from google cloud source request");
    assertThat(outputResult).contains("Number of successfully imported products:");
    assertThat(outputResult).contains("Number of failures during the importing:");
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }
}
