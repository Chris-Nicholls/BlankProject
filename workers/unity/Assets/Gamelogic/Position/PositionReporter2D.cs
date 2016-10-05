using Camera;
using Improbable.Math;
using Improbable.Unity.Visualizer;
using Player;
using UnityEngine;

namespace Position
{
    public class PositionReporter2D : MonoBehaviour
    {
        [Require]
        private SimplePosition.Writer Position;

        private SimplePosition.Update _positionUpdate = new SimplePosition.Update();
        private Coordinates _playerCoordinates;

        public void Update()
        {
            var position = transform.position;
            _playerCoordinates.X = position.x;
            _playerCoordinates.Z = position.y;
            _positionUpdate.SetPosition(_playerCoordinates);
            Position.Send(_positionUpdate);
        }
    }
}
