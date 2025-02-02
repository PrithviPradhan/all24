package org.team100.lib.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.team100.lib.geometry.GeometryUtil;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;

class VisionDataProviderTest {
    private static final double kDelta = 0.01;

    @Test
    void testGetRobotPoseInFieldCoords2() {
        // trivial example: if camera offset happens to match the camera global pose
        // then of course the robot global pose is the origin.
        Transform3d cameraInRobotCoords = new Transform3d(
                new Translation3d(1, 1, 1),
                new Rotation3d(0, 0, 0));
        Pose3d tagInFieldCoords = new Pose3d(2, 1, 1, new Rotation3d(0, 0, 0));
        Transform3d tagInCameraCoords = new Transform3d(new Translation3d(1, 0, 0), new Rotation3d());
        Pose3d cameraInFieldCoords = PoseEstimationHelper.toFieldCoordinates(
                tagInCameraCoords,
                tagInFieldCoords);
        Pose3d robotPoseInFieldCoords = PoseEstimationHelper.applyCameraOffset(
                cameraInFieldCoords,
                cameraInRobotCoords);

        assertEquals(0, robotPoseInFieldCoords.getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getZ(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getZ(), kDelta);
    }

    @Test
    void testGetRobotPoseInFieldCoords3() {
        Transform3d cameraInRobotCoords = new Transform3d(
                new Translation3d(1, 1, 1),
                new Rotation3d(0, 0, 0));
        Pose3d tagInFieldCoords = new Pose3d(2, 1, 1, new Rotation3d(0, 0, 0));
        Translation3d tagTranslationInCameraCoords = new Translation3d(1, 0, 0);
        Rotation3d tagRotationInCameraCoords = new Rotation3d(0, 0, 0);
        Transform3d tagInCameraCoords = new Transform3d(
                tagTranslationInCameraCoords,
                tagRotationInCameraCoords);
        Pose3d cameraInFieldCoords = PoseEstimationHelper.toFieldCoordinates(
                tagInCameraCoords,
                tagInFieldCoords);
        Pose3d robotPoseInFieldCoords = PoseEstimationHelper.applyCameraOffset(
                cameraInFieldCoords,
                cameraInRobotCoords);
        assertEquals(0, robotPoseInFieldCoords.getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getZ(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getZ(), kDelta);
    }

    @Test
    void testGetRobotPoseInFieldCoords4() {
        Transform3d cameraInRobotCoords = new Transform3d(
                new Translation3d(1, 1, 1),
                new Rotation3d(0, 0, 0));
        Pose3d tagInFieldCoords = new Pose3d(2, 1, 1, new Rotation3d(0, 0, 0));

        Rotation3d cameraRotationInFieldCoords = new Rotation3d();

        Blip blip = new Blip(5,
                new double[][] { // pure tilt note we don't actually use this
                        { 1, 0, 0 },
                        { 0, 1, 0 },
                        { 0, 0, 1 } },
                new double[][] { // one meter range (Z forward)
                        { 0 },
                        { 0 },
                        { 1 } });
        Translation3d tagTranslationInCameraCoords = PoseEstimationHelper.blipToTranslation(blip);
        Rotation3d tagRotationInCameraCoords = PoseEstimationHelper.tagRotationInRobotCoordsFromGyro(
                tagInFieldCoords.getRotation(),
                cameraRotationInFieldCoords);
        Transform3d tagInCameraCoords = new Transform3d(
                tagTranslationInCameraCoords,
                tagRotationInCameraCoords);
        Pose3d cameraInFieldCoords = PoseEstimationHelper.toFieldCoordinates(
                tagInCameraCoords,
                tagInFieldCoords);

        Pose3d robotPoseInFieldCoords = PoseEstimationHelper.applyCameraOffset(
                cameraInFieldCoords,
                cameraInRobotCoords);
        assertEquals(0, robotPoseInFieldCoords.getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getZ(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getX(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getY(), kDelta);
        assertEquals(0, robotPoseInFieldCoords.getRotation().getZ(), kDelta);
    }

    @Test
    void testEstimateRobotPose() throws IOException {
        Supplier<Pose2d> robotPose = () -> GeometryUtil.kPoseZero; // always at the origin
        AprilTagFieldLayoutWithCorrectOrientation layout = AprilTagFieldLayoutWithCorrectOrientation
                .redLayout("2024-crescendo.json");
        // VisionDataProvider vdp = new VisionDataProvider(layout, null, robotPose);
        VisionDataProvider24 vdp = new VisionDataProvider24(layout, null, robotPose);

        String key = "foo";
        // in red layout blip 7 is on the other side of the field

        // one meter range (Z forward)
        Blip24 blip = new Blip24(7, new Transform3d(new Translation3d(0, 0, 1), new Rotation3d()));

        // verify tag 5 location
        Pose3d tagPose = layout.getTagPose(7).get();
        assertEquals(16.489, tagPose.getX(), kDelta);
        assertEquals(2.663, tagPose.getY(), kDelta);
        assertEquals(1.451, tagPose.getZ(), kDelta);
        assertEquals(0, tagPose.getRotation().getX(), kDelta);
        assertEquals(0, tagPose.getRotation().getY(), kDelta);
        assertEquals(0, tagPose.getRotation().getZ(), kDelta);

        Blip24[] blips = new Blip24[] {
                blip
        };
        final List<Pose2d> poseEstimate = new ArrayList<Pose2d>();
        final List<Double> timeEstimate = new ArrayList<Double>();
        vdp.estimateRobotPose(
                (p, t) -> {
                    poseEstimate.add(p);
                    timeEstimate.add(t);
                }, key, blips);
        // do it twice to convince vdp it's a good estimate
        vdp.estimateRobotPose(
                (p, t) -> {
                    poseEstimate.add(p);
                    timeEstimate.add(t);
                }, key, blips);
        assertEquals(1, poseEstimate.size());
        assertEquals(1, timeEstimate.size());
        Pose2d result = poseEstimate.get(0);
        assertEquals(15.489, result.getX(), kDelta); // target is one meter in front
        assertEquals(2.663, result.getY(), kDelta); // same y as target
        assertEquals(0, result.getRotation().getRadians(), kDelta); // facing along x
    }

    @Test
    void testEstimateRobotPose2() throws IOException {
        // robot is panned right 45
        Supplier<Pose2d> robotPose = () -> new Pose2d(0, 0, new Rotation2d(-Math.PI / 4)); // just for rotation
        AprilTagFieldLayoutWithCorrectOrientation layout = AprilTagFieldLayoutWithCorrectOrientation
                .redLayout("2024-crescendo.json");
        // VisionDataProvider vdp = new VisionDataProvider(layout, null, robotPose);
        VisionDataProvider24 vdp = new VisionDataProvider24(layout, null, robotPose);

        String key = "foo";
        // in red layout blip 7 is on the other side of the field
        double rot = Math.sqrt(2) / 2;
        // because the tag is nearby we use the tag rotation so make it correct.
        // diagonal
        Blip24 blip = new Blip24(7, new Transform3d(new Translation3d(0, 0, Math.sqrt(2)), new Rotation3d(0, -rot, 0)));
        // verify tag 5 location
        Pose3d tagPose = layout.getTagPose(7).get();
        assertEquals(16.489, tagPose.getX(), kDelta);
        assertEquals(2.663, tagPose.getY(), kDelta);
        assertEquals(1.451, tagPose.getZ(), kDelta);
        assertEquals(0, tagPose.getRotation().getX(), kDelta);
        assertEquals(0, tagPose.getRotation().getY(), kDelta);
        assertEquals(0, tagPose.getRotation().getZ(), kDelta);

        Blip24[] blips = new Blip24[] { blip };
        final List<Pose2d> poseEstimate = new ArrayList<Pose2d>();
        final List<Double> timeEstimate = new ArrayList<Double>();
        vdp.estimateRobotPose(
                (p, t) -> {
                    poseEstimate.add(p);
                    timeEstimate.add(t);
                }, key, blips);
        // two good estimates are required
        vdp.estimateRobotPose(
                (p, t) -> {
                    poseEstimate.add(p);
                    timeEstimate.add(t);
                }, key, blips);
        assertEquals(1, poseEstimate.size());
        assertEquals(1, timeEstimate.size());
        Pose2d result = poseEstimate.get(0);
        assertEquals(15.489, result.getX(), kDelta); // target is one meter in front
        assertEquals(3.663, result.getY(), kDelta); // one meter to the left
        assertEquals(-Math.PI / 4, result.getRotation().getRadians(), kDelta); // facing diagonal
    }
}
