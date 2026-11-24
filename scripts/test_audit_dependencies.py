import io
import unittest
from unittest.mock import patch
import audit_dependencies as scanner

C = {'group': 'example', 'name': 'library', 'version': '1.0', 'purl': 'pkg:maven/example/library@1.0'}


class DependencyAuditTest(unittest.TestCase):
    def test_empty_inventory_is_an_error(self):
        with self.assertRaises(ValueError):
            scanner.components({'components': []})

    def test_incomplete_response_is_an_error(self):
        with patch('urllib.request.urlopen', return_value=io.StringIO('{"results": []}')):
            with self.assertRaises(ValueError):
                scanner.audit([C])

    def test_known_advisory_is_preserved(self):
        response = '{"results": [{"vulns": [{"id": "GHSA-example"}]}]}'
        with patch('urllib.request.urlopen', return_value=io.StringIO(response)):
            result = scanner.audit([C])
        self.assertEqual(result[0]['advisories'], ['GHSA-example'])
        self.assertEqual(result[0]['version'], '1.0')

    def test_network_failure_never_becomes_an_empty_result(self):
        with patch('urllib.request.urlopen', side_effect=OSError('offline')):
            with self.assertRaises(OSError):
                scanner.audit([C])
