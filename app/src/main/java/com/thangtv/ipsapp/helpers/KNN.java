package com.thangtv.ipsapp.helpers;

import com.thangtv.ipsapp.models.KNNPosition;
import com.thangtv.ipsapp.models.KNNRecord;
import com.thangtv.ipsapp.models.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tranvietthang on 12/17/16.
 */

public class KNN {

    public static String classify(List<KNNRecord> trainSet, KNNRecord testData, int k) {


        List<KNNPosition> positions = new ArrayList<>();
        List<KNNRecord> knnRecords = new ArrayList<>();

        for (KNNRecord trainData: trainSet) {
            KNNRecord knnRecord = new KNNRecord(trainData.getX(), trainData.getY(), trainData.getZ());
            knnRecord.setPosition(trainData.getPosition());
            knnRecord.calculateDistanceTo(testData);
            knnRecords.add(knnRecord);
        }

        // find k-nearest neighbors
        for (int i = 0; i < k; i++) {
            KNNRecord closest = new KNNRecord(0, 0, 0);
            closest.setDistance(Float.MAX_VALUE);

            for (KNNRecord record: knnRecords) {
                if (record.getDistance() <= closest.getDistance()) {
                    closest = record;
                }
            }

            knnRecords.remove(closest);

            boolean exited = false;

            if (positions.size() >0) {
                for (KNNPosition position: positions) {
                    if (position.getName().equals(closest.getPosition())) {
                        position.setQuantity(position.getQuantity() + 1);
                        exited = true;
                    }
                }
            }

            if (!exited) {
                KNNPosition position = new KNNPosition();
                position.setName(closest.getPosition());
                position.setQuantity(0);
                positions.add(position);
            }
        }

        // find winning position
        KNNPosition winningPosition = new KNNPosition();
        winningPosition.setQuantity(0);

        for (KNNPosition position: positions) {
            if (position.getQuantity() > winningPosition.getQuantity()) {
                winningPosition = position;
            }
        }

        knnRecords.clear();
        return winningPosition.getName();


    }
}
