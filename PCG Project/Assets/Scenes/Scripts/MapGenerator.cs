using System.Collections;
using System.Collections.Generic;
using System.IO;
using UnityEngine;
using UnityEngine.Tilemaps;
using static Unity.VisualScripting.Member;
using static UnityEditor.IMGUI.Controls.PrimitiveBoundsHandle;
using static UnityEngine.UIElements.UxmlAttributeDescription;

public class MapGenerator : MonoBehaviour
{
    //private int[,] map =
    //{
    //    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
    //    {0, 1, 1, 1, 0, 1, 1, 1, 0, 0},
    //    {0, 0, 0, 1, 0, 1, 0, 1, 0, 0},
    //    {0, 0, 0, 1, 1, 1, 0, 1, 1, 0},
    //    {0, 0, 0, 0, 0, 0, 0, 0, 1, 0},
    //    {0, 0, 0, 0, 0, 0, 1, 1, 1, 0},
    //    {0, 0, 0, 0, 0, 0, 1, 0, 0, 0},
    //    {0, 0, 0, 1, 1, 1, 1, 0, 0, 0},
    //    {0, 1, 1, 1, 0, 0, 0, 0, 0, 0},
    //    {0, 1, 0, 0, 0, 0, 0, 0, 0, 0}
    //};

    //[SerializeField] private GameObject wall;
    //[SerializeField] private GameObject floor;

    [SerializeField] private Tilemap tileMap;
    [SerializeField] private TileData[] tileArray;
    [SerializeField] private TextAsset csvFile;
    private int[,] mapData;

    public int MapWidth { get; set; }
    public int MapHeight { get; set; }

    // Start is called before the first frame update
    void Start()
    {
        //GenerateMap();
        LoadCSVFile();
    }

    //void GenerateMap()
    //{
    //    Vector2 startPos = new Vector2(-MapWidth / 2f + 0.5f, MapHeight / 2f - 0.5f);

    //    for (int y = 0; y < MapHeight; y++)
    //    {
    //        for (int x = 0; x < MapWidth; x++)
    //        {
    //            GameObject tile = map[y, x] == 1 ? floor : wall;
    //            float tileSize = 1f;
    //            Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);    // Generate from the center of the camera
    //            Instantiate(tile, pos, Quaternion.identity);
    //        }
    //    }
    //}

    void LoadCSVFile()
    {
        if (csvFile == null)
        {
            Debug.LogError("No CSV file assigned!");
            return;
        }

        string[] lines = csvFile.text.Split('\n');
        MapHeight = lines.Length;
        MapWidth = lines[0].Split(',').Length;

        mapData = new int[MapHeight, MapWidth];                             // Store tile map coords
        
        for (int y = 0; y < MapHeight; y++)                                 // Each row from top to bottom
        {
            string line = lines[y].Trim();
            if (string.IsNullOrEmpty(line)) continue;

            string[] cells = line.Split(',');

            for (int x = 0; x < cells.Length; x++)                          // Each column from left to right
            {
                int tileIndex = int.Parse(cells[x]);
                if (tileIndex >= 0 && tileIndex < tileArray.Length)
                {
                    Tile tile = ScriptableObject.CreateInstance<Tile>();
                    tile.sprite = tileArray[tileIndex].sprite;
                    tile.colliderType = tileArray[tileIndex].colliderType;

                    Vector3Int pos = new Vector3Int(x, -y, 0);
                    tileMap.SetTile(pos, tile);
                }
            }
        }

        CenterTilemap();
    }

    void CenterTilemap()
    {
        BoundsInt bounds = tileMap.cellBounds;
        Vector3 mapCenter = tileMap.localBounds.center;

        Camera cam = Camera.main;
        cam.transform.position = new Vector3(mapCenter.x - 7, mapCenter.y + 5, cam.transform.position.z);
    }

    //private void OnDrawGizmos()
    //{
    //    if (map == null) return;

    //    float tileSize = 1f;
    //    Vector2 startPos = new Vector2(-MapWidth / 2f + 0.5f, MapHeight / 2f - 0.5f);

    //    for (int y = 0; y < MapHeight; y++)
    //    {
    //        for (int x = 0; x < MapWidth; x++)
    //        {
    //            Vector2 pos = new Vector2(startPos.x + x * tileSize, startPos.y - y * tileSize);
    //            Gizmos.color = map[y, x] == 1 ? Color.green : Color.red;
    //            Gizmos.DrawWireCube(pos, Vector3.one * 0.9f);
    //        }
    //    }
    //}

    // Update is called once per frame
    void Update()
    {
        
    }
}
