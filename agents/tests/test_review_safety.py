import importlib
import json
import os
import pathlib
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1]))
import review_agent

class ReviewSafetyTest(unittest.TestCase):
    def setUp(self): self.ra = importlib.reload(review_agent)

    def test_hidden_and_absolute_paths_are_refused(self):
        for path in ['.git', '.git/config', '/etc/passwd', '../README.md', 'keys/private.pem', '.env.local']:
            with self.subTest(path=path), self.assertRaises(ValueError): self.ra._safe_path(path)

    def test_symlink_even_within_repository_is_refused(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory)
            (root/'file.txt').write_text('data'); (root/'link.txt').symlink_to(root/'file.txt')
            with patch.object(self.ra, 'ROOT', root), self.assertRaises(ValueError): self.ra._safe_path('link.txt')

    def test_invalid_model_shapes_require_manual_review(self):
        for answer in ['[]', 'null', '{"verdict":"ok","constats":"none"}', '{"verdict":"ok","tests_proposes":[1]}', '{"verdict":"ok","constats":[{"gravite":"info","constat":"x","fichier":"../secret"}]}']:
            self.assertEqual('indetermine', self.ra.parse_json(answer)['verdict'])

    def test_missing_key_never_falls_back_to_heuristic_success(self):
        with tempfile.TemporaryDirectory() as directory:
            out=pathlib.Path(directory)/'report.md'
            args=['review_agent.py','--mode','modele','--diff-file',str(self.ra.AGENT_DIR/'evals/cas_nominal.diff'),'--out',str(out)]
            with patch.dict(os.environ, {}, clear=True), patch.object(sys,'argv',args):
                self.assertEqual(2,self.ra.main())
            report=json.loads(out.with_suffix('.json').read_text())
            self.assertEqual('modele',report['trace']['mode'])
            self.assertEqual(0,report['trace']['tours'])
            self.assertEqual('indetermine',report['resultat']['verdict'])

    def test_oversized_diff_is_rejected_before_network_call(self):
        from test_review_agent import fake_anthropic, text
        calls=fake_anthropic([{'stop':'end_turn','content':[text('{"verdict":"ok"}')]}])
        with self.assertRaises(self.ra.AgentStopped): self.ra.run_agent('x'*(self.ra.CFG['max_diff_chars']+1))
        self.assertEqual([],calls)

    def test_budget_rejection_preserves_metrics(self):
        from test_review_agent import fake_anthropic, text
        calls=fake_anthropic([{'stop':'end_turn','content':[text('{"verdict":"ok"}')]}])
        with patch.dict(self.ra.CFG,{'max_cost_usd':0.000001}):
            with self.assertRaises(self.ra.AgentStopped) as stopped: self.ra.run_agent('+ data')
        self.assertEqual(0,stopped.exception.stats['tours']); self.assertEqual([],calls)
