package ai.h2o.automl.targetencoding;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import water.fvec.Frame;
import water.fvec.TestFrameBuilder;
import water.fvec.Vec;
import water.util.TwoDimTable;
import ai.h2o.automl.TestUtil;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TargetEncodingKFoldStrategyTest extends TestUtil {


  @BeforeClass
  public static void setup() {
    stall_till_cloudsize(1);
  }

  private Frame fr = null;


  @Test
  public void prepareEncodingMapForKFoldCaseTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b"))
            .withDataForCol(1, ard(1, 1, 4, 7))
            .withDataForCol(2, ar("2", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 2, 3);

    Frame colAEncoding = targetEncodingMap.get("ColA");

    Vec vec1 = vec(0, 2, 1);
    assertVecEquals(vec1, colAEncoding.vec(2), 1e-5);
    Vec vec2 = vec(1, 2, 1);
    assertVecEquals(vec2, colAEncoding.vec(3), 1e-5);

    vec1.remove();
    vec2.remove();
    encodingMapCleanUp(targetEncodingMap);
  }

  @Test
  public void prepareEncodingMapForKFoldCaseWithSomeOfTheTEValuesRepresentedOnlyInOneFold_Test() {
    //TODO like in te_encoding_possible_bug_demo.R test
  }

  @Test
  public void targetEncoderKFoldHoldoutApplyingTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ard(1, 1, 4, 7, 4))
            .withDataForCol(2, ar("2", "6", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};
    int targetColumnIndex = 2;
    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, targetColumnIndex, 3);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, targetColumnIndex, targetEncodingMap,
            TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 0, 1234, true);

    Vec expected = vec(1, 0, 1, 1, 1);
    assertVecEquals(expected, resultWithEncoding.vec(4), 1e-5);

    expected.remove();
    resultWithEncoding.delete();
    encodingMapCleanUp(targetEncodingMap);
  }

  @Test
  public void getUniqueValuesOfTheFoldColumnTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("fold_column")
            .withVecTypes(Vec.T_NUM)
            .withDataForCol(0, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();

    long[] result = tec.getUniqueValuesOfTheFoldColumn(fr, 0);
    Arrays.sort(result);
    assertArrayEquals(ar(1L, 2L, 3L), result);
  }

  @Test
  public void targetEncoderKFoldHoldout_WithNonFirstColumnToEncode_ApplyingTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColA2", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ar("a", "b", "b", "b", "a"))
            .withDataForCol(2, ard(1, 1, 4, 7, 4))
            .withDataForCol(3, ar("2", "6", "6", "6", "6"))
            .withDataForCol(4, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {1};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 3, 4);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 3, targetEncodingMap,
            TargetEncoder.DataLeakageHandlingStrategy.KFold, 4, false, 0, 1234, true);

    Vec expected = vec(1, 0, 1, 1, 1);
    assertVecEquals(expected, resultWithEncoding.vec(5), 1e-5);

    expected.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
  }

  @Test
  public void targetEncoderKFoldHoldoutApplyingWithoutFoldColumnTest() {
    //TODO fold_column = null case
  }

  @Test
  public void encodingWasCreatedWithFoldsCheckTest() {
    //TODO encoding contains fold column but user did not provide fold column name during application phase.
  }

  @Test
  public void targetEncoderKFoldHoldoutApplyingWithNoiseTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ard(1, 1, 4, 7, 4))
            .withDataForCol(2, ar("2", "6", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2))
            .build();

    Frame fr2 = new TestFrameBuilder()
            .withName("testFrame2")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "c", "b", "a"))
            .withDataForCol(1, ard(1, 1, 4, 7, 4))
            .withDataForCol(2, ar("2", "6", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 2, 3);

    printOutFrameAsTable(targetEncodingMap.get("ColA"), true, true);
    //If we do not pass noise_level as parameter then it will be calculated according to the type of target column. For categorical target column it defaults to 1e-2
    Frame resultWithEncoding = tec.applyTargetEncoding(fr2, teColumns, 2, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 1234, true);

    // We expect that for `c` level we will get mean of encoded column i.e. 0.75 +- noise
    Vec expected = vec(0.75, 1.0, 0.0, 1, 1);
    assertVecEquals(expected, resultWithEncoding.vec(4), 1e-2); // TODO is it ok that encoding contains negative values?

    expected.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
    fr2.delete();
  }

  @Test
  public void targetEncoderKFoldHoldoutApplyingWithCustomNoiseTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ard(1, 1, 4, 7, 4))
            .withDataForCol(2, ar("2", "6", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 2, 3);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 2, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 0.02, 1234, true);

    TwoDimTable resultTable = resultWithEncoding.toTwoDimTable();
    System.out.println("Result table" + resultTable.toString());
    Vec expected = vec(1, 0, 1, 1, 1);
    assertVecEquals(expected, resultWithEncoding.vec(4), 2e-2); // TODO we do not check here actually that we have noise more then default 0.01. We need to check that sometimes we get 0.01 < delta < 0.02

    expected.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
  }

  @Test
  public void targetEncoderKFoldHoldoutApplyingWithMulticlassTargetColumnTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ar(1, 2, 3, 4, 5))
            .withDataForCol(2, ar(1, 2, 2, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 1, 2);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 1, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 2, false, 0, 1234, true);

    printOutFrameAsTable(resultWithEncoding);
    Vec expected = vec(5.0, 1.0, 4.0, 4.0, 2.5);
    assertVecEquals(expected, resultWithEncoding.vec("ColA_te"), 1e-5);

    expected.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
  }

  @Ignore
  @Test
  public void targetEncoderKFoldHoldoutApplyingWithBlendedAvgTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_NUM, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a", "c"))
            .withDataForCol(1, ard(1, 1, 4, 7, 4, 9))
            .withDataForCol(2, ar("2", "6", "6", "6", "6", "2"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 2, 3);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 2, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, true, 0.0, 1234, true);

    Vec encodedVec = resultWithEncoding.vec(4);

    // TODO I'm not sure if the values are correct but we at least can fix them and avoid regression while changing code further.
    assertEquals(0.855, encodedVec.at(0), 1e-3);
    assertEquals(0.724, encodedVec.at(1), 1e-3);
    assertEquals(0.855, encodedVec.at(2), 1e-3);
    assertEquals(0.856, encodedVec.at(4), 1e-3);

    encodedVec.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
  }


  @Test
  public void manualHighCardinalityKFoldTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "c", "c", "a", "d", "d", "d", "d", "e", "e", "a", "f", "f"))
            .withDataForCol(1, ar("2", "6", "6", "6", "6", "6", "2", "6", "6", "6", "6", "2", "2", "2", "2"))
            .withDataForCol(2, ar(1, 2, 1, 2, 1, 3, 2, 2, 1, 3, 1, 2, 3, 3, 2))
            .build();

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0};

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 1, 2);

    printOutFrameAsTable(targetEncodingMap.get("ColA"))
    ;
    //If we do not pass noise_level as parameter then it will be calculated according to the type of target column. For categorical target column it defaults to 1e-2
    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 1, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 2, false, 0.0, 1234, true);

    printOutFrameAsTable(resultWithEncoding, true, true);
    double expectedDifferenceDueToNoise = 1e-5;
    Vec vec = resultWithEncoding.vec(3);
    Vec expected = vec(0.5, 0, 0, 1, 1, 1, 1, 0.66666, 1, 1, 0.66666, 0, 1, 0, 0);
    assertVecEquals(expected, vec, expectedDifferenceDueToNoise);

    expected.remove();
    encodingMapCleanUp(targetEncodingMap);
    resultWithEncoding.delete();
  }

  // ------------------------ Multiple columns for target encoding -------------------------------------------------//

  @Test
  public void KFoldHoldoutMultipleTEColumnsWithFoldColumnTest() {
    TestFrameBuilder frameBuilder = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB", "ColC", "fold_column")
            .withVecTypes(Vec.T_CAT, Vec.T_CAT, Vec.T_CAT, Vec.T_NUM)
            .withDataForCol(0, ar("a", "b", "b", "b", "a"))
            .withDataForCol(1, ar("d", "e", "d", "e", "e"))
            .withDataForCol(2, ar("2", "6", "6", "6", "6"))
            .withDataForCol(3, ar(1, 2, 2, 3, 2));

    TargetEncoder tec = new TargetEncoder();
    int[] teColumns = {0, 1};

    fr = frameBuilder.withName("testFrame").build();

    Map<String, Frame> targetEncodingMap = tec.prepareEncodingMap(fr, teColumns, 2, 3);

    Frame resultWithEncoding = tec.applyTargetEncoding(fr, teColumns, 2, targetEncodingMap, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 0, 1234, true);
    Frame sortedBy2 = resultWithEncoding.sort(new int[]{2});
    Vec encodingForColumnA_Multiple = sortedBy2.vec(4);
    Frame sortedBy0 = resultWithEncoding.sort(new int[]{0});
    Vec encodingForColumnB_Multiple = sortedBy0.vec(5);

    //Let's check it with Single TE version of the algorithm. So we rely here on a correctness of the single-column encoding.
    //  For the first encoded column
    Frame frA = frameBuilder.withName("testFrameA").build();

    int[] indexForColumnA = Arrays.copyOfRange(teColumns, 0, 1);
    Map<String, Frame> targetEncodingMapForColumnA = tec.prepareEncodingMap(frA, indexForColumnA, 2, 3);
    Frame resultWithEncodingForColumnA = tec.applyTargetEncoding(frA, indexForColumnA, 2, targetEncodingMapForColumnA, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 0, 1234, true);
    Vec encodingForColumnA_Single = resultWithEncodingForColumnA.vec(4);

    assertVecEquals(encodingForColumnA_Single, encodingForColumnA_Multiple, 1e-5);

    // For the second encoded column
    Frame frB = frameBuilder.withName("testFrameB").build();

    int[] indexForColumnB = Arrays.copyOfRange(teColumns, 1, 2);
    Map<String, Frame> targetEncodingMapForColumnB = tec.prepareEncodingMap(frB, indexForColumnB, 2, 3);
    Frame resultWithEncodingForColumnB = tec.applyTargetEncoding(frB, indexForColumnB, 2, targetEncodingMapForColumnB, TargetEncoder.DataLeakageHandlingStrategy.KFold, 3, false, 0, 1234, true);
    Frame sortedByColA = resultWithEncodingForColumnB.sort(new int[]{0});
    Vec encodingForColumnB_Single = sortedByColA.vec(4);
    assertVecEquals(encodingForColumnB_Single, encodingForColumnB_Multiple, 1e-5);


    sortedBy0.delete();
    sortedBy2.delete();
    sortedByColA.delete();
    encodingMapCleanUp(targetEncodingMap);
    encodingMapCleanUp(targetEncodingMapForColumnA);
    encodingMapCleanUp(targetEncodingMapForColumnB);
    frA.delete();
    frB.delete();
    resultWithEncoding.delete();
    resultWithEncodingForColumnA.delete();
    resultWithEncodingForColumnB.delete();
  }

  @Test
  public void targetEncoderGetOutOfFoldDataTest() {
    fr = new TestFrameBuilder()
            .withName("testFrame")
            .withColNames("ColA", "ColB")
            .withVecTypes(Vec.T_NUM, Vec.T_NUM)
            .withDataForCol(0, ard(5, 6, 7, 9))
            .withDataForCol(1, ard(1, 2, 3, 1))
            .build();

    TargetEncoder tec = new TargetEncoder();

    Frame outOfFoldData = tec.getOutOfFoldData(fr, "ColB", 1);
    TwoDimTable twoDimTable = outOfFoldData.toTwoDimTable();
    assertEquals(outOfFoldData.numRows(), 2);

    assertEquals(6L, twoDimTable.get(5, 0));
    assertEquals(7L, twoDimTable.get(6, 0));

    Frame outOfFoldData2 = tec.getOutOfFoldData(fr, "ColB", 2);
    TwoDimTable twoDimTable2 = outOfFoldData2.toTwoDimTable();

    assertEquals(5L, twoDimTable2.get(5, 0));
    assertEquals(7L, twoDimTable2.get(6, 0));
    assertEquals(9L, twoDimTable2.get(7, 0));

    outOfFoldData.delete();
    outOfFoldData2.delete();
  }

  @After
  public void afterEach() {
    if (fr != null) fr.delete();
  }

  private void encodingMapCleanUp(Map<String, Frame> encodingMap) {
    for (Map.Entry<String, Frame> map : encodingMap.entrySet()) {
      map.getValue().delete();
    }
  }

  private void printOutFrameAsTable(Frame fr) {

    TwoDimTable twoDimTable = fr.toTwoDimTable();
    System.out.println(twoDimTable.toString(2, false));
  }

  private void printOutFrameAsTable(Frame fr, boolean full, boolean rollups) {

    TwoDimTable twoDimTable = fr.toTwoDimTable(0, 10000, rollups);
    System.out.println(twoDimTable.toString(2, full));
  }

}