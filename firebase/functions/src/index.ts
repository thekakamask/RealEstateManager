/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

import {Bucket} from "@google-cloud/storage";
import {setGlobalOptions} from "firebase-functions/v2";
import {onSchedule} from "firebase-functions/v2/scheduler";
import * as logger from "firebase-functions/logger";
// import {onRequest} from "firebase-functions/https";
// import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

// Init Admin SDK (obligatoire pour bypass les rules)
admin.initializeApp();

// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
// setGlobalOptions({ maxInstances: 10 });
setGlobalOptions({
  maxInstances: 1, // important : unique job.
});

const THIRTY_DAYS_MS = 30 * 24 * 60 * 60 * 1000;
const BATCH_LIMIT = 450;

const COLLECTIONS = [
  "properties",
  "photos",
  "static_maps",
  "pois",
  "property_poi_cross_refs",
];

/**
 * Scheduled cleanup job.
 *
 * Hard deletes Firestore documents and Storage files that have been
 * soft-deleted (isDeleted = true) for more than 30 days.
 *
 * Runs once per week to keep Firebase clean and prevent zombie data.
 */
export const cleanupSoftDeleteData = onSchedule(
  {
    schedule: "0 3 * * 0",
    timeZone: "Europe/Paris",
    region: "europe-west1",
  },
  async () => {
    const db = admin.firestore();
    const bucket = admin.storage().bucket();

    const now = Date.now();
    const cutoff = now - THIRTY_DAYS_MS;

    logger.info("🧹 Cleanup job started", {cutoff});

    for (const collectionName of COLLECTIONS) {
      logger.info(`🔍 Scanning collection: ${collectionName}`);

      const snapshot = await db
        .collection(collectionName)
        .where("isDeleted", "==", true)
        .where("updatedAt", "<", cutoff)
        .get();

      if (snapshot.empty) {
        logger.info(`✅ No documents to clean in ${collectionName}`);
        continue;
      }

      let batch = db.batch();
      let batchCount = 0;


      for (const doc of snapshot.docs) {
        const docId = doc.id;

        try {
          // 🔥 Delete associated Storage files if needed
          if (collectionName === "photos") {
            await deleteFileIfExists(bucket, `photos/${docId}.jpg`);
          }

          if (collectionName === "static_maps") {
            await deleteFileIfExists(bucket, `static_maps/${docId}.jpg`);
          }

          batch.delete(doc.ref);
          batchCount++;

          if (batchCount >= BATCH_LIMIT) {
            await batch.commit();
            logger.info("📦 Firestore batch committed", {
              collection: collectionName,
              count: batchCount,
            });
            batch = db.batch();
            batchCount = 0;
          }
        } catch (error) {
          logger.error("❌ Failed to schedule deletion", {
            collection: collectionName,
            docId,
            error,
          });
        }
      }

      if (batchCount > 0) {
        await batch.commit();
        logger.info("📦 Final Firestore batch committed", {
          collection: collectionName,
          count: batchCount,
        });
      }
    }

    logger.info("✅ Cleanup job finished successfully");
  }
);

// export const helloWorld = onRequest((request, response) => {
//   logger.info("Hello logs!", {structuredData: true});
//   response.send("Hello from Firebase!");
// });

// ---------- Helpers ----------


/**
 * Deletes a file from Firebase Storage if it exists.
 *
 * Used during cleanup of soft-deleted entities to remove
 * orphaned image files safely.
 *
 * @param {Bucket} bucket Firebase Storage bucket
 * @param {string} path Path of the file in storage
 */
async function deleteFileIfExists(
  bucket: Bucket,
  path: string
) {
  const file = bucket.file(path);

  const [exists] = await file.exists();
  if (exists) {
    await file.delete();
    logger.info("🗑️ Storage file deleted", {path});
  } else {
    logger.info("ℹ️ Storage file not found", {path});
  }
}