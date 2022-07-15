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

package events.setup;

import static setup.SetupCleanup.deleteBucket;
import static setup.SetupCleanup.deleteDataset;

import com.google.api.gax.rpc.PermissionDeniedException;
import com.google.cloud.ServiceOptions;
import com.google.cloud.retail.v2.*;
import com.google.cloud.retail.v2.ProductServiceClient.ListProductsPagedResponse;
import java.io.IOException;

public class RemoveEventsResources {

  public static void main(String[] args) throws IOException {
    String projectId = ServiceOptions.getDefaultProjectId();
    String bucketName = System.getenv("EVENTS_BUCKET_NAME");
    String branchName =
        String.format(
            "projects/%s/locations/global/catalogs/default_catalog/branches/0", projectId);

    deleteBucket(bucketName);
    deleteAllEvents(branchName);
    deleteDataset(projectId, "user_events");
  }

  public static void deleteAllEvents(String branchName) throws IOException {
    System.out.println("Deleting events in process, please wait...");

    try (UserEventServiceClient eventServiceClient = UserEventServiceClient.create()) {
      PurgeUserEventsRequest purgeUserEventsRequest = PurgeUserEventsRequest.newBuilder().setParent(branchName).build();
      eventServiceClient.purgeUserEventsAsync(purgeUserEventsRequest);
      System.out.printf("Events were deleted from %s%n", branchName);
    }
  }

  public static void deleteAllProducts(String branchName) throws IOException {
    System.out.println("Deleting products in process, please wait...");

    try (ProductServiceClient productServiceClient = ProductServiceClient.create()) {
      ListProductsRequest listRequest =
          ListProductsRequest.newBuilder().setParent(branchName).build();
      ListProductsPagedResponse products = productServiceClient.listProducts(listRequest);

      int deleteCount = 0;

      for (Product product : products.iterateAll()) {
        DeleteProductRequest deleteRequest =
            DeleteProductRequest.newBuilder().setName(product.getName()).build();

        try {
          productServiceClient.deleteProduct(deleteRequest);
          deleteCount++;
        } catch (PermissionDeniedException e) {
          System.out.println(
              "Ignore PermissionDenied in case the product does not exist "
                  + "at time of deletion.");
        }
      }

      System.out.printf("%s products were deleted from %s%n", deleteCount, branchName);
    }
  }
}
